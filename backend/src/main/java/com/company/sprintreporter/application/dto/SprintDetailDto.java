package com.company.sprintreporter.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * Pre-parsed sprint metadata exposed to the frontend so it does not need to parse sprint names.
 */
@Getter
@Builder
@Jacksonized
public class SprintDetailDto {
    /** PI identifier, e.g. "26.3" */
    private final String pi;
    /** Iteration number within the PI (1-4), or 99 for IP week */
    private final int iteration;
    /** True if this sprint is an IP (Innovation & Planning) week */
    private final boolean ip;
}
