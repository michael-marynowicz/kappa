package com.company.sprintreporter.service;

import com.company.sprintreporter.application.dto.*;
import com.company.sprintreporter.config.MetricsProperties;
import com.company.sprintreporter.domain.model.IterationSnapshot;
import com.company.sprintreporter.domain.model.SprintIssue;
import com.company.sprintreporter.domain.port.CapacityPlanningStore;
import com.company.sprintreporter.domain.port.JiraIssueRepository;
import com.company.sprintreporter.domain.port.RemainingStoryPointsStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final JiraIssueRepository jiraIssueRepository;
    private final RemainingStoryPointsStore remainingStoryPointsStore;
    private final MetricsProperties metricsProperties;
    private final CapacityService capacityService;

    public SprintMetricsResponseDto getSprintMetrics(UUID organizationId) {
        List<SprintIssue> issues = fetchMergedIssues(organizationId);

        // VELOCITY: only count issues committed at sprint start (exclude mid-sprint additions)
        List<SprintIssue> committedIssues = issues.stream()
                .filter(i -> !i.isAddedAfterSprintStart())
                .toList();

        int committed = committedIssues.stream()
                .mapToInt(i -> i.getTotalStoryPoints() != null ? i.getTotalStoryPoints() : 0)
                .sum();

        // Delivered = SP of DONE issues from the committed set only
        int delivered = committedIssues.stream()
                .filter(SprintIssue::isCompleted)
                .mapToInt(i -> i.getTotalStoryPoints() != null ? i.getTotalStoryPoints() : 0)
                .sum();

        int leftover = committed - delivered;
        int work = delivered;
        double ratio = committed > 0 ? Math.round((double) delivered / committed * 1000.0) / 10.0 : 0;

        // Total issue count (all issues including mid-sprint additions)
        int totalIssueCount = issues.size();

        // Topics: aggregate ALL issues (including mid-sprint) by business category
        List<TopicBreakdownDto> topicBreakdown = computeTopicBreakdown(issues);
        String activeSprint = capacityService.getActiveSprintName();
        CapacityDto capacity = capacityService.computeCapacity(organizationId, activeSprint);
        double realCapacity = round2(capacity.getRealCapacity());
        TeamAvailabilityDto teamAvailability = capacityService.computeTeamAvailability(organizationId, activeSprint);

        return SprintMetricsResponseDto.builder()
                .committedStoryPoints(committed)
                .deliveredStoryPoints(delivered)
                .workStoryPoints(work)
                .leftoverStoryPoints(leftover)
                .ratio(ratio)
                .issueCount(totalIssueCount)
                .topicBreakdown(topicBreakdown)
                .realCapacity(realCapacity)
                .teamAvailability(teamAvailability)
                .build();
    }

    public List<IterationSnapshotDto> getIterationSnapshots(UUID organizationId) {
        Map<String, List<SprintIssue>> closedSprints = jiraIssueRepository.fetchClosedSprintIssues();

        // Also include current sprint
        List<SprintIssue> currentIssues = fetchMergedIssues(organizationId);

        List<IterationSnapshotDto> snapshots = closedSprints.entrySet().stream()
                .map(entry -> buildSnapshot(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        snapshots.add(buildSnapshot("Current Sprint", currentIssues));

        return snapshots;
    }

    /**
     * Computes average metrics across all tracked sprints.
     * Includes normalizedVelocity (delivered SP / real capacity) when capacity data exists.
     * This enforces the business rule: average normalized velocity requires capacity data.
     */
    public AverageMetricsResponseDto getAverageMetrics(UUID organizationId) {
        List<IterationSnapshotDto> iterations = getIterationSnapshots(organizationId);

        double avgVelocity = iterations.stream()
                .mapToInt(IterationSnapshotDto::getVelocity)
                .average()
                .orElse(0.0);

        double avgRatio = iterations.stream()
                .mapToDouble(IterationSnapshotDto::getRatio)
                .average()
                .orElse(0.0);

        // Compute normalized velocity using capacity data (if available)
        Double avgNormalizedVelocity = computeAverageNormalizedVelocity(organizationId, iterations);

        return AverageMetricsResponseDto.builder()
                .averageVelocity(Math.round(avgVelocity * 10.0) / 10.0)
                .averageRatio(Math.round(avgRatio * 10.0) / 10.0)
                .averageNormalizedVelocity(avgNormalizedVelocity)
                .sprintCount(iterations.size())
                .iterations(iterations)
                .build();
    }

    /**
     * Computes average velocity normalized by capacity across sprints.
     * Returns null if no capacity data is available (guards against incoherent data).
     */
    private Double computeAverageNormalizedVelocity(UUID organizationId, List<IterationSnapshotDto> iterations) {
        double totalNormalized = 0;
        int countWithCapacity = 0;

        for (IterationSnapshotDto iteration : iterations) {
            CapacityDto capacity = capacityService.computeCapacity(organizationId, iteration.getSprintName());
            if (capacity != null && capacity.getRealCapacity() > 0) {
                double normalized = (double) iteration.getVelocity() / capacity.getRealCapacity();
                totalNormalized += normalized;
                countWithCapacity++;
            }
        }

        if (countWithCapacity == 0) {
            return null; // No capacity data available — cannot compute
        }

        return Math.round((totalNormalized / countWithCapacity) * 100.0) / 100.0;
    }

    private IterationSnapshotDto buildSnapshot(String sprintName, List<SprintIssue> issues) {
        // Only count issues committed at sprint start for velocity
        List<SprintIssue> committedIssues = issues.stream()
                .filter(i -> !i.isAddedAfterSprintStart())
                .toList();

        int committed = committedIssues.stream()
                .mapToInt(i -> i.getTotalStoryPoints() != null ? i.getTotalStoryPoints() : 0)
                .sum();

        int delivered = committedIssues.stream()
                .filter(SprintIssue::isCompleted)
                .mapToInt(i -> i.getTotalStoryPoints() != null ? i.getTotalStoryPoints() : 0)
                .sum();

        double ratio = committed > 0 ? Math.round((double) delivered / committed * 1000.0) / 10.0 : 0;

        return IterationSnapshotDto.builder()
                .sprintName(sprintName)
                .committedStoryPoints(committed)
                .deliveredStoryPoints(delivered)
                .velocity(delivered)
                .ratio(ratio)
                .build();
    }

    private List<TopicBreakdownDto> computeTopicBreakdown(List<SprintIssue> issues) {
        // Group by topic: SP sum + issue count
        Map<String, Integer> topicPoints = issues.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getTopic() != null ? i.getTopic() : "Other",
                        Collectors.summingInt(i -> i.getTotalStoryPoints() != null ? i.getTotalStoryPoints() : 0)
                ));
        Map<String, Long> topicCounts = issues.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getTopic() != null ? i.getTopic() : "Other",
                        Collectors.counting()
                ));

        int totalPoints = topicPoints.values().stream().mapToInt(Integer::intValue).sum();

        return topicPoints.entrySet().stream()
                .map(e -> TopicBreakdownDto.builder()
                        .topic(e.getKey())
                        .storyPoints(e.getValue())
                        .percentage(totalPoints > 0
                                ? Math.round((double) e.getValue() / totalPoints * 1000.0) / 10.0
                                : 0.0)
                        .issueCount(topicCounts.getOrDefault(e.getKey(), 0L).intValue())
                        .build())
                .sorted((a, b) -> Integer.compare(b.getStoryPoints(), a.getStoryPoints()))
                .toList();
    }

    private List<SprintIssue> fetchMergedIssues(UUID organizationId) {
        List<SprintIssue> issues = jiraIssueRepository.fetchSprintIssues();
        var overrides = remainingStoryPointsStore.findAll(organizationId);
        return issues.stream()
                .map(issue -> {
                    Integer overrideValue = overrides.get(issue.getIssueKey());
                    if (overrideValue != null) {
                        return issue.withRemainingStoryPoints(overrideValue);
                    }
                    // Default: remaining = total (0 done) when not overridden
                    if (issue.getRemainingStoryPoints() == null && issue.getTotalStoryPoints() != null) {
                        return issue.withRemainingStoryPoints(issue.getTotalStoryPoints());
                    }
                    return issue;
                })
                .toList();
    }

        private double round2(double value) {
                return BigDecimal.valueOf(value)
                                .setScale(2, RoundingMode.HALF_UP)
                                .doubleValue();
        }
}
