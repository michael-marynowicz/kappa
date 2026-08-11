package com.company.sprintreporter.application.dto.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class PlanResponseDto {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private Integer maxUsers;
    private Integer maxDashboards;
    private BigDecimal priceMonthly;
    private BigDecimal priceYearly;
    private Integer trialDays;
    private List<String> features;
    private boolean contactOnly;
}
