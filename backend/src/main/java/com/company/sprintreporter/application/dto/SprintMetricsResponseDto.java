package com.company.sprintreporter.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * Response DTO exposing all sprint-level Scrum Master metrics.
 * Consumed by the frontend analytics section and CSV export.
 */
@Getter
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SprintMetricsResponseDto {

    // Story Points
    private final int committedStoryPoints;
    private final int completedStoryPoints;
    private final int remainingStoryPoints;
    private final int spilloverStoryPoints;

    // Velocity & Predictability
    private final int velocity;
    private final Double predictabilityRate;
    private final Boolean sprintSuccess;

    // Issue Counts
    private final int totalIssues;
    private final int completedIssues;
    private final int inProgressIssues;
    private final int todoIssues;
    private final int blockedIssues;
    private final int bugCount;
    private final int storyCount;
    private final int taskCount;

    // Ratios & Rates
    private final Double blockedRatio;
    private final Double bugRatio;
    private final Double deliveredVsCommittedRatio;
    private final Double sprintFocusFactor;
    private final Double teamEfficiency;
    private final Double averageSpPerCompletedIssue;
    private final Double sprintHealthScore;

    // Throughput & Flow
    private final int throughput;
    private final int workInProgress;
    private final int carryOverIssues;
}
