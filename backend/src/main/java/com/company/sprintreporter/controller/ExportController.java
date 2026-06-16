package com.company.sprintreporter.controller;

import com.company.sprintreporter.domain.model.SprintIssue;
import com.company.sprintreporter.domain.model.SprintMetrics;
import com.company.sprintreporter.service.CsvExportService;
import com.company.sprintreporter.service.SprintIssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller: Export resource.
 * Generates enriched CSV with full SM metrics + issue detail.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
public class ExportController {

    private final SprintIssueService sprintIssueService;
    private final CsvExportService csvExportService;

    @GetMapping("/csv")
    public ResponseEntity<byte[]> exportCsv() {
        log.info("GET /api/v1/export/csv - generating enriched sprint report CSV");

        List<SprintIssue> issues  = sprintIssueService.getIssuesForExport();
        SprintMetrics metrics     = sprintIssueService.getMetrics();
        String csvContent         = csvExportService.exportToCsv(issues, metrics);
        byte[] csvBytes           = csvContent.getBytes(StandardCharsets.UTF_8);

        String filename = "sprint-report-%s.csv".formatted(LocalDate.now());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .contentLength(csvBytes.length)
                .body(csvBytes);
    }
}
