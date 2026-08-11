package com.company.sprintreporter.service;

import com.company.sprintreporter.application.dto.EpicSummaryDto;
import com.company.sprintreporter.application.dto.SprintIssueResponseDto;
import com.company.sprintreporter.domain.model.SprintIssue;
import com.company.sprintreporter.domain.port.JiraIssueRepository;
import com.company.sprintreporter.domain.port.RemainingStoryPointsStore;
import com.company.sprintreporter.service.exception.IssueNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Core service: orchestrates domain logic.
 * All business decisions live here, NOT in controllers or infrastructure.
 *
 * Responsibilities:
 * - Fetch issues from Jira (via port, agnostic of implementation)
 * - Merge with user-provided remaining SP overrides
 * - Validate business rules before persisting
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SprintIssueService {

    private final JiraIssueRepository jiraIssueRepository;
    private final RemainingStoryPointsStore remainingStoryPointsStore;

    /**
     * Retrieve all sprint issues, merging Jira data with any stored remaining SP overrides.
     */
    public List<SprintIssue> getSprintIssues(UUID organizationId) {
        log.debug("Fetching sprint issues from Jira repository");
        List<SprintIssue> issues = jiraIssueRepository.fetchSprintIssues();

        // Merge persisted remaining SP overrides onto the fetched issues
        var overrides = remainingStoryPointsStore.findAll(organizationId);
        List<SprintIssue> merged = issues.stream()
                .map(issue -> {
                    Integer overrideValue = overrides.get(issue.getIssueKey());
                                        SprintIssue mergedIssue = issue;
                    if (overrideValue != null) {
                                                mergedIssue = issue.withRemainingStoryPoints(overrideValue);
                                        } else if (issue.getRemainingStoryPoints() == null && issue.getTotalStoryPoints() != null) {
                                                // Default: remaining = total (0 done) when not overridden
                                                mergedIssue = issue.withRemainingStoryPoints(issue.getTotalStoryPoints());
                    }

                                        // Coherence rule: a completed issue cannot keep remaining SP.
                                        if (mergedIssue.isCompleted() && mergedIssue.getTotalStoryPoints() != null) {
                                                return mergedIssue.withRemainingStoryPoints(0);
                    }
                                        return mergedIssue;
                })
                .toList();

        log.debug("Returning {} issues ({} with SP overrides)", merged.size(), overrides.size());
        return merged;
    }

    /**
     * Update the remaining story points for a given issue key.
     * Validates that the new value does not exceed total story points.
     */
    public SprintIssue updateRemainingStoryPoints(UUID organizationId, String issueKey, int remainingStoryPoints) {
        log.info("Updating remaining SP for issue {} to {}", issueKey, remainingStoryPoints);

        // Fetch fresh issue data to validate against current total
        List<SprintIssue> issues = jiraIssueRepository.fetchSprintIssues();
        SprintIssue issue = issues.stream()
                .filter(i -> i.getIssueKey().equals(issueKey))
                .findFirst()
                .orElseThrow(() -> new IssueNotFoundException(
                        "Issue with key '%s' not found in current sprint".formatted(issueKey)));

        // Re-create the issue with new remaining SP — enforces domain invariants
        SprintIssue updated = SprintIssue.create(
                issue.getIssueKey(),
                issue.getSummary(),
                issue.getStatus(),
                issue.getAssignee(),
                issue.getIssueType(),
                issue.getTopic(),
                issue.getTotalStoryPoints(),
                remainingStoryPoints
        );

        remainingStoryPointsStore.save(organizationId, issueKey, remainingStoryPoints);
        log.info("Remaining SP updated and persisted for issue {}", issueKey);
        return updated;
    }

    /**
     * Return sprint issues grouped by epic (topic).
     * Each epic shows aggregated SP and contains its child stories.
     */
    public List<EpicSummaryDto> getIssuesGroupedByEpic(UUID organizationId) {
        List<SprintIssue> issues = getSprintIssues(organizationId);

        Map<String, List<SprintIssue>> byEpic = issues.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getTopic() != null ? i.getTopic() : "Other",
                        java.util.LinkedHashMap::new,
                        Collectors.toList()
                ));

        return byEpic.entrySet().stream()
                .map(entry -> {
                    List<SprintIssue> epicIssues = entry.getValue();

                    int total = epicIssues.stream()
                            .mapToInt(i -> i.getTotalStoryPoints() != null ? i.getTotalStoryPoints() : 0)
                            .sum();
                    int done = epicIssues.stream()
                            .mapToInt(i -> i.getDoneStoryPoints() != null ? i.getDoneStoryPoints() : 0)
                            .sum();
                    int remaining = total - done;
                    double completion = total > 0
                            ? Math.round((double) done / total * 1000.0) / 10.0
                            : 0.0;

                    List<SprintIssueResponseDto> childDtos = epicIssues.stream()
                            .map(i -> SprintIssueResponseDto.builder()
                                    .issueKey(i.getIssueKey())
                                    .summary(i.getSummary())
                                    .status(i.getStatus())
                                    .assignee(i.getAssignee())
                                    .issueType(i.getIssueType())
                                    .topic(i.getTopic())
                                    .totalStoryPoints(i.getTotalStoryPoints())
                                    .remainingStoryPoints(i.getRemainingStoryPoints())
                                    .doneStoryPoints(i.getDoneStoryPoints())
                                    .build())
                            .toList();

                    return EpicSummaryDto.builder()
                            .epicName(entry.getKey())
                            .issueCount(epicIssues.size())
                            .totalStoryPoints(total)
                            .doneStoryPoints(done)
                            .remainingStoryPoints(remaining)
                            .completionPercentage(completion)
                            .issues(childDtos)
                            .build();
                })
                .sorted((a, b) -> Integer.compare(b.getTotalStoryPoints(), a.getTotalStoryPoints()))
                .toList();
    }

    /**
     * Return all sprint issues with computed KPIs, ready for CSV export.
     * This is intentionally the same as getSprintIssues(); the export controller
     * handles the formatting concern.
     */
    public List<SprintIssue> getIssuesForExport(UUID organizationId) {
        return getSprintIssues(organizationId);
    }
}
