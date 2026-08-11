package com.company.sprintreporter.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@Builder
@Jacksonized
public class SprintMetricsResponseDto {
    private final int committedStoryPoints;
    private final int deliveredStoryPoints;
    private final int workStoryPoints;
    private final int leftoverStoryPoints;
    private final double ratio;

    /** Total number of issues in the sprint (committed + added mid-sprint). */
    private final int issueCount;

    private final List<TopicBreakdownDto> topicBreakdown;
    private final double realCapacity;
    private final TeamAvailabilityDto teamAvailability;
}
