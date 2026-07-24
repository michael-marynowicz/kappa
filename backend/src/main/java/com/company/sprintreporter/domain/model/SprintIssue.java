package com.company.sprintreporter.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

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
    private final String statusCategoryKey;
    private final String assignee;
    private final String issueType;
    private final String topic;
    private final Integer totalStoryPoints;
    private final Integer remainingStoryPoints;
    private final boolean addedAfterSprintStart;

    /**
     * Core business computation: done = total - remaining.
     * If the issue is completed, done = total regardless of remainingSP.
     * If total or remaining is unknown and not completed, done is unknown.
     */
    public Integer getDoneStoryPoints() {
        if (totalStoryPoints == null) {
            return null;
        }
        if (isCompleted()) {
            return totalStoryPoints;
        }
        if (remainingStoryPoints == null) {
            return 0; // Default: nothing done yet (remaining = total)
        }
        return Math.max(0, totalStoryPoints - remainingStoryPoints);
    }

    public boolean isCompleted() {
        return "Done".equalsIgnoreCase(status)
                || "Completed".equalsIgnoreCase(status)
                || "done".equalsIgnoreCase(statusCategoryKey);
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
            String topic,
            Integer totalStoryPoints,
            Integer remainingStoryPoints) {
        return create(issueKey, summary, status, assignee, issueType, topic,
                totalStoryPoints, remainingStoryPoints, false);
    }

    public static SprintIssue create(
            String issueKey,
            String summary,
            String status,
            String assignee,
            String issueType,
            String topic,
            Integer totalStoryPoints,
            Integer remainingStoryPoints,
            boolean addedAfterSprintStart) {

        if (issueKey == null || issueKey.isBlank()) {
            throw new IllegalArgumentException("Issue key must not be blank");
        }

        var issue = SprintIssue.builder()
                .issueKey(issueKey)
                .summary(summary)
                .status(status)
                .assignee(assignee)
                .issueType(issueType)
                .topic(topic)
                .totalStoryPoints(totalStoryPoints)
                .remainingStoryPoints(remainingStoryPoints)
                .addedAfterSprintStart(addedAfterSprintStart)
                .build();

        if (!issue.isValid()) {
            throw new IllegalArgumentException(
                    "Remaining story points (%d) cannot exceed total (%d) for issue %s"
                            .formatted(remainingStoryPoints, totalStoryPoints, issueKey));
        }

        return issue;
    }
}
