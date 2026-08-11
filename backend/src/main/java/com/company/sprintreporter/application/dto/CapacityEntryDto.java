package com.company.sprintreporter.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class CapacityEntryDto {
    @NotBlank(message = "Team member ID must not be blank")
    private final String teamMemberId;

    @NotBlank(message = "Sprint name must not be blank")
    private final String sprintName;

    @Min(value = 0, message = "Days off must be >= 0")
    private final double daysOff;
}
