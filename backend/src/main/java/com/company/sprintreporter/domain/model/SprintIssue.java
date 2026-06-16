package com.company.sprintreporter.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.util.Objects;

/**
 * Core domain model representing a Jira issue within a sprint.
 * This is the heart of the domain — intentionally decoupled from any
 * framework or infrastructure concern.
 *
 * Business invariants are enforced here, not in services or controllers.
 */
@Getter
@Builder
@With
public class SprintIssue {

    private final String issueKey;
    private final String summary;
    private final String status;
    private final String assignee;
    private final String issueType;
    private final Integer totalStoryPoints;
    private final Integer remainingStoryPoints;

    /**
     * Core business computation: done = total - remaining.
     * If status is Done, done = total (all points are considered delivered).
     * If total is unknown, done is unknown too.
     */
    public Integer getDoneStoryPoints() {
        if (totalStoryPoints == null) {
            return null;
        }
        if ("Done".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status)) {
            return totalStoryPoints;
        }
        // If remaining SP has not been entered yet, we can't compute done SP
        if (remainingStoryPoints == null) {
            return 0;
        }
        return Math.max(0, totalStoryPoints - remainingStoryPoints);
    }

    /** Displayed remaining = total - done (always consistent with doneStoryPoints). */
    public Integer getDisplayedRemainingStoryPoints() {
        if (totalStoryPoints == null) return null;
        Integer done = getDoneStoryPoints();
        if (done == null) return totalStoryPoints;
        return Math.max(0, totalStoryPoints - done);
    }

    /**
     * Business rule: remaining cannot exceed total story points.
     */
    public boolean isValid() {
        if (totalStoryPoints == null || remainingStoryPoints == null) {
            return true; // partial data is acceptable
        }
        return remainingStoryPoints >= 0 && remainingStoryPoints <= totalStoryPoints;
    }

    /**
     * Factory method enforcing business constraints on creation.
     */
    public static SprintIssue create(
            String issueKey,
            String summary,
            String status,
            String assignee,
            String issueType,
            Integer totalStoryPoints,
            Integer remainingStoryPoints) {

        if (issueKey == null || issueKey.isBlank()) {
            throw new IllegalArgumentException("Issue key must not be blank");
        }

        var issue = SprintIssue.builder()
                .issueKey(issueKey)
                .summary(summary)
                .status(status)
                .assignee(assignee)
                .issueType(issueType)
                .totalStoryPoints(totalStoryPoints)
                .remainingStoryPoints(remainingStoryPoints)
                .build();

        if (!issue.isValid()) {
            throw new IllegalArgumentException(
                    "Remaining story points (%d) cannot exceed total (%d) for issue %s"
                            .formatted(remainingStoryPoints, totalStoryPoints, issueKey));
        }

        return issue;
    }
}
