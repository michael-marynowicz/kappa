package com.company.sprintreporter.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

@Getter
@Builder
@With
public class CapacityEntry {

    private final String teamMemberId;
    private final String sprintName;
    private final double daysOff;

    public static CapacityEntry create(String teamMemberId, String sprintName, double daysOff) {
        if (teamMemberId == null || teamMemberId.isBlank()) {
            throw new IllegalArgumentException("Team member ID must not be blank");
        }
        if (sprintName == null || sprintName.isBlank()) {
            throw new IllegalArgumentException("Sprint name must not be blank");
        }
        if (daysOff < 0) {
            throw new IllegalArgumentException("Days off cannot be negative");
        }
        return CapacityEntry.builder()
                .teamMemberId(teamMemberId)
                .sprintName(sprintName)
                .daysOff(daysOff)
                .build();
    }
}
