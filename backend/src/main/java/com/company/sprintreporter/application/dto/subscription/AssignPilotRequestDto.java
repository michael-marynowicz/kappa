package com.company.sprintreporter.application.dto.subscription;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class AssignPilotRequestDto {

    @NotBlank
    private String planCode;

    @NotNull
    @Future
    private Instant pilotExpiresAt;
}
