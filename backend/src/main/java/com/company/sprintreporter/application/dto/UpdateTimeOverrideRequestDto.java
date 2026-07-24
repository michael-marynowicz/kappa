package com.company.sprintreporter.application.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class UpdateTimeOverrideRequestDto {

    @NotNull(message = "timeOverride must be provided")
    @DecimalMin(value = "0.0", message = "timeOverride must be >= 0")
    @DecimalMax(value = "1.0", message = "timeOverride must be <= 1")
    private final Double timeOverride;
}
