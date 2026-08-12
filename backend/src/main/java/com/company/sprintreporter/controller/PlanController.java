package com.company.sprintreporter.controller;

import com.company.sprintreporter.application.dto.plan.PlanResponseDto;
import com.company.sprintreporter.service.subscription.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class PlanController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<List<PlanResponseDto>> getPlans() {
        var plans = subscriptionService.getAvailablePlans().stream()
                .map(plan -> PlanResponseDto.builder()
                        .id(plan.getId())
                        .code(plan.getCode())
                        .name(plan.getDisplayName())
                        .description(plan.getDescription())
                        .maxUsers(plan.getMaxMembers() == -1 ? null : plan.getMaxMembers())
                        .maxDashboards(plan.getMaxDashboards() == -1 ? null : plan.getMaxDashboards())
                        .priceMonthly(plan.getPriceMonthly())
                        .priceYearly(plan.getPriceYearly())
                        .currency("EUR")
                        .trialDays(plan.getTrialDays())
                        .features(plan.getFeatures().stream().map(f -> f.getCode()).toList())
                        .contactOnly("enterprise".equals(plan.getCode()))
                        .build())
                .toList();
        return ResponseEntity.ok(plans);
    }
}
