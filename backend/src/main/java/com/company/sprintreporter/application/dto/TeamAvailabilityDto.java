package com.company.sprintreporter.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class TeamAvailabilityDto {
    private final double dev;
    private final double pda;
    private final double qa;
}
