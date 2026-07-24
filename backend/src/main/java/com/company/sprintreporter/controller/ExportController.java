package com.company.sprintreporter.controller;

import com.company.sprintreporter.application.dto.CapacityPlanningResponseDto;
import com.company.sprintreporter.config.feature.FeatureCode;
import com.company.sprintreporter.config.feature.RequiresFeature;
import com.company.sprintreporter.config.jwt.JwtAuthenticationToken;
import com.company.sprintreporter.domain.model.SprintIssue;
import com.company.sprintreporter.service.CapacityService;
import com.company.sprintreporter.service.CsvExportService;
import com.company.sprintreporter.service.SprintIssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller: Export resource.
 *
 * Handles CSV export generation.
 * Delegates all business logic to the service layer.
 * Only concern here: correct HTTP headers for file download.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
public class ExportController {

    private final SprintIssueService sprintIssueService;
    private final CsvExportService csvExportService;
    private final CapacityService capacityService;

    /**
     * GET /api/v1/export/csv
     * Returns sprint issues as a downloadable CSV file.
     * The filename includes today's date for easy archiving.
     */
    @GetMapping("/csv")
    @RequiresFeature(FeatureCode.CSV_EXPORT)
    public ResponseEntity<byte[]> exportCsv() {
        log.info("GET /api/v1/export/csv - generating sprint report CSV");

        List<SprintIssue> issues = sprintIssueService.getIssuesForExport(getOrgId());
        String csvContent = csvExportService.exportToCsv(issues);
        byte[] csvBytes = csvContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        String filename = "sprint-report-%s.csv".formatted(LocalDate.now());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .contentLength(csvBytes.length)
                .body(csvBytes);
    }

    @GetMapping("/capacity/csv")
    @RequiresFeature(FeatureCode.CAPACITY_PLANNING)
    public ResponseEntity<byte[]> exportCapacityCsv() {
        log.info("GET /api/v1/export/capacity/csv - generating capacity report CSV");

        CapacityPlanningResponseDto capacity = capacityService.getCapacityPlanning(getOrgId());
        String csvContent = csvExportService.exportCapacityToCsv(capacity);
        byte[] csvBytes = csvContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        String filename = "capacity-report-%s.csv".formatted(LocalDate.now());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .contentLength(csvBytes.length)
                .body(csvBytes);
    }

    private UUID getOrgId() {
        return ((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getOrganizationId();
    }
}
