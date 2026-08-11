package com.company.sprintreporter.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the SprintIssue domain model.
 * No Spring context needed — pure domain logic testing.
 */
@DisplayName("SprintIssue domain model")
class SprintIssueTest {

    @Nested
    @DisplayName("getDoneStoryPoints()")
    class GetDoneStoryPoints {

        @ParameterizedTest(name = "total={0}, remaining={1} → done={2}")
        @CsvSource({
            "8, 0, 8",
            "8, 3, 5",
            "5, 5, 0",
            "13, 1, 12"
        })
        @DisplayName("should compute done = total - remaining")
        void computesDoneCorrectly(int total, int remaining, int expectedDone) {
            SprintIssue issue = SprintIssue.builder()
                    .issueKey("SCRUM-1")
                    .summary("Test issue")
                    .totalStoryPoints(total)
                    .remainingStoryPoints(remaining)
                    .build();

            assertThat(issue.getDoneStoryPoints()).isEqualTo(expectedDone);
        }

        @Test
        @DisplayName("should return null when totalStoryPoints is null")
        void returnsNullWhenTotalIsNull() {
            SprintIssue issue = SprintIssue.builder()
                    .issueKey("SCRUM-1")
                    .totalStoryPoints(null)
                    .remainingStoryPoints(null)
                    .build();

            assertThat(issue.getDoneStoryPoints()).isNull();
        }

        @Test
        @DisplayName("should treat null remaining as zero done")
        void treatsNullRemainingAsZero() {
            SprintIssue issue = SprintIssue.builder()
                    .issueKey("SCRUM-1")
                    .totalStoryPoints(8)
                    .remainingStoryPoints(null)
                    .build();

            assertThat(issue.getDoneStoryPoints()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("SprintIssue.create() factory")
    class Create {

        @Test
        @DisplayName("should throw when issueKey is blank")
        void throwsOnBlankIssueKey() {
            assertThatThrownBy(() ->
                SprintIssue.create("", "Summary", "Done", "Alice", "Story", null, 5, 2)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("Issue key must not be blank");
        }

        @Test
        @DisplayName("should throw when remaining exceeds total")
        void throwsWhenRemainingExceedsTotal() {
            assertThatThrownBy(() ->
                SprintIssue.create("SCRUM-1", "Summary", "To Do", "Bob", "Story", null, 5, 8)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("cannot exceed total");
        }

        @Test
        @DisplayName("should succeed with valid data")
        void createsIssueSuccessfully() {
            SprintIssue issue = SprintIssue.create(
                    "SCRUM-42", "Do the thing", "In Progress", "Carol", "Story", "Backend", 8, 3);

            assertThat(issue.getIssueKey()).isEqualTo("SCRUM-42");
            assertThat(issue.getDoneStoryPoints()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("isValid()")
    class IsValid {

        @Test
        @DisplayName("should be valid when remaining <= total")
        void validWhenRemainingLteTotal() {
            SprintIssue issue = SprintIssue.builder()
                    .issueKey("SCRUM-1")
                    .totalStoryPoints(8)
                    .remainingStoryPoints(3)
                    .build();

            assertThat(issue.isValid()).isTrue();
        }

        @Test
        @DisplayName("should be invalid when remaining > total")
        void invalidWhenRemainingGtTotal() {
            SprintIssue issue = SprintIssue.builder()
                    .issueKey("SCRUM-1")
                    .totalStoryPoints(5)
                    .remainingStoryPoints(9)
                    .build();

            assertThat(issue.isValid()).isFalse();
        }

        @Test
        @DisplayName("should be valid when both are null")
        void validWhenBothNull() {
            SprintIssue issue = SprintIssue.builder()
                    .issueKey("SCRUM-1")
                    .totalStoryPoints(null)
                    .remainingStoryPoints(null)
                    .build();

            assertThat(issue.isValid()).isTrue();
        }
    }
}
