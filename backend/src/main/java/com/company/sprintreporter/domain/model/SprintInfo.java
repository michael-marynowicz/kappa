package com.company.sprintreporter.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Getter
@Builder
public class SprintInfo {

    private final String name;
    private final LocalDate startDate;
    private final LocalDate endDate;

    /**
     * Calculate the number of business days (Mon-Fri) in the sprint.
     */
    public int getBusinessDays() {
        if (startDate == null || endDate == null) {
            return 10; // default fallback
        }
        int count = 0;
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                count++;
            }
            date = date.plusDays(1);
        }
        return count;
    }
}
