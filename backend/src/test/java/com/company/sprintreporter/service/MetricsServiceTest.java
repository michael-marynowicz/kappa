package com.company.sprintreporter.service;

import com.company.sprintreporter.application.dto.CapacityDto;
import com.company.sprintreporter.application.dto.SprintMetricsResponseDto;
import com.company.sprintreporter.application.dto.TeamAvailabilityDto;
import com.company.sprintreporter.application.dto.TopicBreakdownDto;
import com.company.sprintreporter.config.MetricsProperties;
import com.company.sprintreporter.domain.model.SprintIssue;
import com.company.sprintreporter.domain.port.JiraIssueRepository;
import com.company.sprintreporter.domain.port.RemainingStoryPointsStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MetricsService")
class MetricsServiceTest {

    @Mock
    private JiraIssueRepository jiraIssueRepository;
    @Mock
    private RemainingStoryPointsStore remainingStoryPointsStore;
    @Mock
    private MetricsProperties metricsProperties;
    @Mock
    private CapacityService capacityService;

    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        metricsService = new MetricsService(
                jiraIssueRepository,
                remainingStoryPointsStore,
                metricsProperties,
                capacityService
        );
    }

    @Nested
    @DisplayName("Velocity calculation")
    class VelocityCalculation {

        @Test
        @DisplayName("should exclude mid-sprint additions from committed count")
        void excludesMidSprintFromCommitted() {
            List<SprintIssue> issues = List.of(
                    sprintIssue("A-1", "Done", 8, false),
                    sprintIssue("A-2", "In Progress", 5, false),
                    sprintIssue("A-3", "Done", 3, true)  // added mid-sprint
            );
            stubDependencies(issues);

            SprintMetricsResponseDto metrics = metricsService.getSprintMetrics(UUID.randomUUID());

            // Committed = 8 + 5 = 13 (excludes A-3 which was added mid-sprint)
            assertThat(metrics.getCommittedStoryPoints()).isEqualTo(13);
        }

        @Test
        @DisplayName("should only count DONE issues for delivered")
        void onlyDoneForDelivered() {
            List<SprintIssue> issues = List.of(
                    sprintIssue("A-1", "Done", 8, false),
                    sprintIssue("A-2", "In Progress", 5, false),
                    sprintIssue("A-3", "To Do", 3, false)
            );
            stubDependencies(issues);

            SprintMetricsResponseDto metrics = metricsService.getSprintMetrics(UUID.randomUUID());

            // Delivered = 8 (only Done issues)
            assertThat(metrics.getDeliveredStoryPoints()).isEqualTo(8);
        }

        @Test
        @DisplayName("should compute leftover as committed minus delivered")
        void leftoverIsCommittedMinusDelivered() {
            List<SprintIssue> issues = List.of(
                    sprintIssue("A-1", "Done", 8, false),
                    sprintIssue("A-2", "In Progress", 5, false)
            );
            stubDependencies(issues);

            SprintMetricsResponseDto metrics = metricsService.getSprintMetrics(UUID.randomUUID());

            // Committed=13, Delivered=8, Leftover=5
            assertThat(metrics.getLeftoverStoryPoints()).isEqualTo(5);
        }

        @Test
        @DisplayName("should compute ratio as delivered/committed percentage")
        void ratioIsDeliveredOverCommitted() {
            List<SprintIssue> issues = List.of(
                    sprintIssue("A-1", "Done", 8, false),
                    sprintIssue("A-2", "In Progress", 5, false),
                    sprintIssue("A-3", "Done", 3, false)
            );
            stubDependencies(issues);

            SprintMetricsResponseDto metrics = metricsService.getSprintMetrics(UUID.randomUUID());

            // Committed=16, Delivered=11, Ratio=68.8%
            assertThat(metrics.getRatio()).isEqualTo(68.8);
        }

        @Test
        @DisplayName("should not count mid-sprint DONE issues in delivered")
        void midSprintDoneNotCounted() {
            List<SprintIssue> issues = List.of(
                    sprintIssue("A-1", "Done", 8, false),
                    sprintIssue("A-2", "Done", 5, true)  // done but added mid-sprint
            );
            stubDependencies(issues);

            SprintMetricsResponseDto metrics = metricsService.getSprintMetrics(UUID.randomUUID());

            assertThat(metrics.getCommittedStoryPoints()).isEqualTo(8);
            assertThat(metrics.getDeliveredStoryPoints()).isEqualTo(8);
            assertThat(metrics.getLeftoverStoryPoints()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Topic breakdown")
    class TopicBreakdown {

        @Test
        @DisplayName("should keep raw topic names")
        void keepsRawTopicNames() {
            List<SprintIssue> issues = List.of(
                    issue("A-1", "Backend", 8),
                    issue("A-2", "Frontend", 5),
                    issue("A-3", "Security", 3),
                    issue("A-4", "DevOps", 13),
                    issue("A-5", "Quality", 5)
            );
            stubDependencies(issues);

            SprintMetricsResponseDto metrics = metricsService.getSprintMetrics(UUID.randomUUID());
            List<TopicBreakdownDto> topics = metrics.getTopicBreakdown();

            assertThat(topics).extracting(TopicBreakdownDto::getTopic)
                    .containsExactlyInAnyOrder("Backend", "Frontend", "Security", "DevOps", "Quality");
        }

        @Test
        @DisplayName("should compute percentage that sums to 100")
        void percentagesSumTo100() {
            List<SprintIssue> issues = List.of(
                    issue("A-1", "Backend", 8),
                    issue("A-2", "Frontend", 5),
                    issue("A-3", "QA", 7)
            );
            stubDependencies(issues);

            SprintMetricsResponseDto metrics = metricsService.getSprintMetrics(UUID.randomUUID());
            double totalPct = metrics.getTopicBreakdown().stream()
                    .mapToDouble(TopicBreakdownDto::getPercentage)
                    .sum();

            assertThat(totalPct).isCloseTo(100.0, org.assertj.core.data.Offset.offset(0.5));
        }

        @Test
        @DisplayName("should sort by story points descending")
        void sortedByPointsDescending() {
            List<SprintIssue> issues = List.of(
                    issue("A-1", "Backend", 3),
                    issue("A-2", "Frontend", 13),
                    issue("A-3", "QA", 8)
            );
            stubDependencies(issues);

            SprintMetricsResponseDto metrics = metricsService.getSprintMetrics(UUID.randomUUID());
            List<Integer> points = metrics.getTopicBreakdown().stream()
                    .map(TopicBreakdownDto::getStoryPoints)
                    .toList();

            assertThat(points).isSortedAccordingTo((a, b) -> Integer.compare(b, a));
        }
    }

    private void stubDependencies(List<SprintIssue> issues) {
        when(jiraIssueRepository.fetchSprintIssues()).thenReturn(issues);
        when(remainingStoryPointsStore.findAll(any(UUID.class))).thenReturn(Map.of());
        when(capacityService.getActiveSprintName()).thenReturn("Current Sprint");
        when(capacityService.computeCapacity(any(UUID.class), anyString()))
                .thenReturn(CapacityDto.builder().plannedCapacity(100).realCapacity(90).build());
        when(capacityService.computeTeamAvailability(any(UUID.class), anyString()))
                .thenReturn(TeamAvailabilityDto.builder().dev(5).pda(2).qa(3).build());
    }

    private SprintIssue sprintIssue(String key, String status, int sp, boolean addedMidSprint) {
        return SprintIssue.builder()
                .issueKey(key)
                .summary("Test " + key)
                .status(status)
                .issueType("Story")
                .topic("Backend")
                .totalStoryPoints(sp)
                .addedAfterSprintStart(addedMidSprint)
                .build();
    }

    private SprintIssue issue(String key, String topic, int sp) {
        return SprintIssue.builder()
                .issueKey(key)
                .summary("Test " + key)
                .status("Done")
                .issueType("Story")
                .topic(topic)
                .totalStoryPoints(sp)
                .build();
    }
}
