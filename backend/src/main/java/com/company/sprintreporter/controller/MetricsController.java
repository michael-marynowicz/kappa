package com.company.sprintreporter.controller;

import com.company.sprintreporter.application.dto.AverageMetricsResponseDto;
import com.company.sprintreporter.application.dto.IterationSnapshotDto;
import com.company.sprintreporter.application.dto.SprintMetricsResponseDto;
import com.company.sprintreporter.config.feature.FeatureCode;
import com.company.sprintreporter.config.feature.RequiresFeature;
import com.company.sprintreporter.config.jwt.JwtAuthenticationToken;
import com.company.sprintreporter.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    @GetMapping
    @RequiresFeature(FeatureCode.METRICS_DASHBOARD)
    public ResponseEntity<SprintMetricsResponseDto> getSprintMetrics() {
        log.debug("GET /api/v1/metrics");
        return ResponseEntity.ok(metricsService.getSprintMetrics(getOrgId()));
    }

    @GetMapping("/iterations")
    @RequiresFeature(FeatureCode.ITERATION_COMPARISON)
    public ResponseEntity<List<IterationSnapshotDto>> getIterationSnapshots() {
        log.debug("GET /api/v1/metrics/iterations");
        return ResponseEntity.ok(metricsService.getIterationSnapshots(getOrgId()));
    }

    /**
     * Advanced average metrics: requires both iteration comparison AND capacity planning.
     * Computes average velocity normalized by capacity across sprints.
     * Returns 403 if the plan lacks either feature.
     */
    @GetMapping("/average")
    @RequiresFeature({FeatureCode.ITERATION_COMPARISON, FeatureCode.CAPACITY_PLANNING})
    public ResponseEntity<AverageMetricsResponseDto> getAverageMetrics() {
        log.debug("GET /api/v1/metrics/average");
        return ResponseEntity.ok(metricsService.getAverageMetrics(getOrgId()));
    }

    private UUID getOrgId() {
        return ((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getOrganizationId();
    }
}
