package com.company.sprintreporter.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.List;

/**
 * Standardized error response body for all API errors.
 * Consistent error contract regardless of exception type.
 */
@Getter
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponseDto {

    private final int status;
    private final String error;
    private final String message;
    private final List<String> details;
    private final String errorCode;

    @Builder.Default
    private final Instant timestamp = Instant.now();
}
