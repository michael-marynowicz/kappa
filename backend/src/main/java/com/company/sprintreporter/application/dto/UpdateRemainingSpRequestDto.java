package com.company.sprintreporter.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Request DTO for updating remaining story points on an issue.
 * Validation annotations prevent bad data from reaching the service layer.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRemainingSpRequestDto {

    @NotBlank(message = "Issue key must not be blank")
    private String issueKey;

    @NotNull(message = "Remaining story points must be provided")
    @Min(value = 0, message = "Remaining story points cannot be negative")
    private Integer remainingStoryPoints;
}
