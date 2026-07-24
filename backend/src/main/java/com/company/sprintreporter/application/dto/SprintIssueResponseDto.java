package com.company.sprintreporter.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * Response DTO for a sprint issue.
 * Intentionally decoupled from the domain model —
 * adding/removing fields here doesn't break the domain.
 */
@Getter
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SprintIssueResponseDto {

    private final String issueKey;
    private final String summary;
    private final String status;
    private final String assignee;
    private final String issueType;
    private final String topic;
    private final Integer totalStoryPoints;
    private final Integer remainingStoryPoints;
    private final Integer doneStoryPoints;
}
