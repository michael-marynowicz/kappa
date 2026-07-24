package com.company.sprintreporter.service;

import com.company.sprintreporter.domain.model.SprintIssue;
import com.company.sprintreporter.domain.port.JiraIssueRepository;
import com.company.sprintreporter.domain.port.RemainingStoryPointsStore;
import com.company.sprintreporter.service.exception.IssueNotFoundException;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SprintIssueService")
class SprintIssueServiceTest {

    @Mock
    private JiraIssueRepository jiraIssueRepository;

    @Mock
    private RemainingStoryPointsStore remainingStoryPointsStore;

    @InjectMocks
    private SprintIssueService service;

    private SprintIssue sampleIssue;
    private final UUID orgId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        sampleIssue = SprintIssue.builder()
                .issueKey("SCRUM-1")
                .summary("Sample story")
                .status("In Progress")
                .assignee("Alice")
                .issueType("Story")
                .totalStoryPoints(8)
                .remainingStoryPoints(null)
                .build();
    }

    @Nested
    @DisplayName("getSprintIssues()")
    class GetSprintIssues {

        @Test
        @DisplayName("should merge remaining SP overrides from store onto fetched issues")
        void mergesStoreOverridesOntoFetchedIssues() {
            when(jiraIssueRepository.fetchSprintIssues()).thenReturn(List.of(sampleIssue));
            when(remainingStoryPointsStore.findAll(any(UUID.class))).thenReturn(Map.of("SCRUM-1", 3));

            List<SprintIssue> result = service.getSprintIssues(orgId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRemainingStoryPoints()).isEqualTo(3);
            assertThat(result.get(0).getDoneStoryPoints()).isEqualTo(5); // 8 - 3
        }

        @Test
        @DisplayName("should default remaining to total when no overrides exist")
        void returnsIssuesUnchangedWithNoOverrides() {
            when(jiraIssueRepository.fetchSprintIssues()).thenReturn(List.of(sampleIssue));
            when(remainingStoryPointsStore.findAll(any(UUID.class))).thenReturn(Map.of());

            List<SprintIssue> result = service.getSprintIssues(orgId);

            assertThat(result.get(0).getRemainingStoryPoints()).isEqualTo(8);
        }

        @Test
        @DisplayName("should force remaining to zero for completed issues")
        void forcesRemainingToZeroForCompletedIssues() {
            SprintIssue doneIssue = SprintIssue.builder()
                    .issueKey("SCRUM-2")
                    .summary("Done story")
                    .status("Done")
                    .assignee("Bob")
                    .issueType("Story")
                    .totalStoryPoints(13)
                    .remainingStoryPoints(null)
                    .build();

            when(jiraIssueRepository.fetchSprintIssues()).thenReturn(List.of(doneIssue));
            when(remainingStoryPointsStore.findAll(any(UUID.class))).thenReturn(Map.of());

            List<SprintIssue> result = service.getSprintIssues(orgId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRemainingStoryPoints()).isEqualTo(0);
            assertThat(result.get(0).getDoneStoryPoints()).isEqualTo(13);
        }

        @Test
        @DisplayName("should keep remaining zero for completed issues even with override")
        void keepsCompletedIssueRemainingAtZeroEvenWithOverride() {
            SprintIssue doneIssue = SprintIssue.builder()
                    .issueKey("SCRUM-3")
                    .summary("Done story")
                    .status("Done")
                    .assignee("Bob")
                    .issueType("Story")
                    .totalStoryPoints(8)
                    .remainingStoryPoints(8)
                    .build();

            when(jiraIssueRepository.fetchSprintIssues()).thenReturn(List.of(doneIssue));
            when(remainingStoryPointsStore.findAll(any(UUID.class))).thenReturn(Map.of("SCRUM-3", 5));

            List<SprintIssue> result = service.getSprintIssues(orgId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRemainingStoryPoints()).isEqualTo(0);
            assertThat(result.get(0).getDoneStoryPoints()).isEqualTo(8);
        }
    }

    @Nested
    @DisplayName("updateRemainingStoryPoints()")
    class UpdateRemainingStoryPoints {

        @Test
        @DisplayName("should persist valid remaining SP and return updated issue")
        void persistsAndReturnsUpdatedIssue() {
            when(jiraIssueRepository.fetchSprintIssues()).thenReturn(List.of(sampleIssue));

            SprintIssue result = service.updateRemainingStoryPoints(orgId, "SCRUM-1", 5);

            assertThat(result.getRemainingStoryPoints()).isEqualTo(5);
            assertThat(result.getDoneStoryPoints()).isEqualTo(3); // 8 - 5
            verify(remainingStoryPointsStore).save(orgId, "SCRUM-1", 5);
        }

        @Test
        @DisplayName("should throw IssueNotFoundException when issue key does not exist")
        void throwsWhenIssueKeyNotFound() {
            when(jiraIssueRepository.fetchSprintIssues()).thenReturn(List.of(sampleIssue));

            assertThatThrownBy(() -> service.updateRemainingStoryPoints(orgId, "SCRUM-999", 2))
                    .isInstanceOf(IssueNotFoundException.class)
                    .hasMessageContaining("SCRUM-999");

            verify(remainingStoryPointsStore, never()).save(any(UUID.class), any(), anyInt());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when remaining exceeds total")
        void throwsWhenRemainingExceedsTotal() {
            when(jiraIssueRepository.fetchSprintIssues()).thenReturn(List.of(sampleIssue));

            // sampleIssue has totalStoryPoints = 8, so 10 should fail
            assertThatThrownBy(() -> service.updateRemainingStoryPoints(orgId, "SCRUM-1", 10))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(remainingStoryPointsStore, never()).save(any(UUID.class), any(), anyInt());
        }
    }
}
