package com.company.sprintreporter.controller;

import com.company.sprintreporter.application.dto.SprintMetricsResponseDto;
import com.company.sprintreporter.application.mapper.SprintMetricsMapper;
import com.company.sprintreporter.domain.model.SprintMetrics;
import com.company.sprintreporter.service.SprintIssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller: Metrics resource.
 * Exposes aggregated sprint KPIs for the analytics section.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final SprintIssueService sprintIssueService;
    private final SprintMetricsMapper metricsMapper;

    /**
     * GET /api/v1/metrics
     * Returns all computed sprint-level Scrum Master metrics.
     */
    @GetMapping
    public ResponseEntity<SprintMetricsResponseDto> getMetrics() {
        log.debug("GET /api/v1/metrics");
        SprintMetrics metrics = sprintIssueService.getMetrics();
        return ResponseEntity.ok(metricsMapper.toResponseDto(metrics));
    }
}
