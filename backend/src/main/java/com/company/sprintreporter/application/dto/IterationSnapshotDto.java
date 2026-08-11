package com.company.sprintreporter.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IterationSnapshotDto {
    private final String sprintName;
    private final int committedStoryPoints;
    private final int deliveredStoryPoints;
    private final int velocity;
    private final double ratio;

    /** Velocity normalized by capacity (delivered SP / real capacity). Null if capacity data unavailable. */
    private final Double normalizedVelocity;
}
