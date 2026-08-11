package com.company.sprintreporter.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class CapacityDto {
    private final double plannedCapacity;
    private final double realCapacity;
}
