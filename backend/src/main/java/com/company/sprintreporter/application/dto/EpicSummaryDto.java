package com.company.sprintreporter.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EpicSummaryDto {

    private final String epicName;
    private final int issueCount;
    private final int totalStoryPoints;
    private final int doneStoryPoints;
    private final int remainingStoryPoints;
    private final double completionPercentage;
    private final List<SprintIssueResponseDto> issues;
}
