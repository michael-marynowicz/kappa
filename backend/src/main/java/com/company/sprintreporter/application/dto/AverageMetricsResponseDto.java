package com.company.sprintreporter.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * Advanced metrics response requiring both iteration_comparison + capacity_planning features.
 * Includes average velocity normalized by capacity across past sprints.
 */
@Getter
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AverageMetricsResponseDto {

    /** Average velocity (delivered SP) across all tracked sprints */
    private final double averageVelocity;

    /** Average ratio (delivered/committed %) across all tracked sprints */
    private final double averageRatio;

    /** Average velocity normalized by real capacity (SP per capacity unit). Null if no capacity data. */
    private final Double averageNormalizedVelocity;

    /** Number of sprints included in the computation */
    private final int sprintCount;

    /** Per-sprint breakdown */
    private final List<IterationSnapshotDto> iterations;
}
