package com.company.sprintreporter.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@Jacksonized
public class CapacityPlanningResponseDto {
    private final List<TeamMemberDto> members;
    private final List<String> sprints;
    // Map<sprintName, businessDays> — calculated from sprint start/end dates
    private final Map<String, Integer> daysPerSprint;
    // Map<teamMemberId, Map<sprintName, daysOff>>
    private final Map<String, Map<String, Double>> daysOffGrid;
    // Map<sprintName, {pi, iteration, ip}> — pre-parsed for frontend consumption
    private final Map<String, SprintDetailDto> sprintDetails;
}
