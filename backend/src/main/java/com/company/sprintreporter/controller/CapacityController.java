package com.company.sprintreporter.controller;

import com.company.sprintreporter.application.dto.*;
import com.company.sprintreporter.config.feature.FeatureCode;
import com.company.sprintreporter.config.feature.RequiresFeature;
import com.company.sprintreporter.config.jwt.JwtAuthenticationToken;
import com.company.sprintreporter.service.CapacityService;
import com.company.sprintreporter.service.CsvExportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/capacity")
@RequiredArgsConstructor
@RequiresFeature(FeatureCode.CAPACITY_PLANNING)
public class CapacityController {

    private final CapacityService capacityService;
    private final CsvExportService csvExportService;

    // ── Full grid (used to render the Capacity tab) ──

    @GetMapping
    public ResponseEntity<CapacityPlanningResponseDto> getCapacityPlanning() {
        log.debug("GET /api/v1/capacity");
        return ResponseEntity.ok(capacityService.getCapacityPlanning(getOrgId()));
    }

    // ── Team Members CRUD ──

    @GetMapping("/members")
    public ResponseEntity<List<TeamMemberDto>> getMembers() {
        log.debug("GET /api/v1/capacity/members");
        return ResponseEntity.ok(capacityService.getAllMembers(getOrgId()));
    }

    @PostMapping("/members")
    public ResponseEntity<TeamMemberDto> addMember(@Valid @RequestBody TeamMemberDto dto) {
        log.info("POST /api/v1/capacity/members — adding {}", dto.getName());
        return ResponseEntity.ok(capacityService.addMember(getOrgId(), dto));
    }

    @PutMapping("/members/{id}")
    public ResponseEntity<TeamMemberDto> updateMember(@PathVariable String id, @Valid @RequestBody TeamMemberDto dto) {
        log.info("PUT /api/v1/capacity/members/{} — updating", id);
        return ResponseEntity.ok(capacityService.updateMember(getOrgId(), id, dto));
    }

    @RequestMapping(value = "/members/{id}/time-override", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<TeamMemberDto> updateMemberTimeOverride(
            @PathVariable String id,
            @Valid @RequestBody UpdateTimeOverrideRequestDto dto) {
        log.info("{} /api/v1/capacity/members/{}/time-override", "PUT/PATCH", id);
        return ResponseEntity.ok(capacityService.updateMemberTimeOverride(getOrgId(), id, dto.getTimeOverride()));
    }

    @DeleteMapping("/members/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable String id) {
        log.info("DELETE /api/v1/capacity/members/{}", id);
        capacityService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }

    // ── Days Off ──

    @PutMapping("/days-off")
    public ResponseEntity<CapacityEntryDto> setDaysOff(@Valid @RequestBody CapacityEntryDto dto) {
        log.info("PUT /api/v1/capacity/days-off — {} / {} = {} days",
                dto.getTeamMemberId(), dto.getSprintName(), dto.getDaysOff());
        return ResponseEntity.ok(capacityService.setDaysOff(getOrgId(), dto));
    }

    @PutMapping("/days-off/bulk")
    public ResponseEntity<Void> setBulkDaysOff(@Valid @RequestBody List<CapacityEntryDto> entries) {
        log.info("PUT /api/v1/capacity/days-off/bulk — {} entries", entries.size());
        capacityService.setBulkDaysOff(getOrgId(), entries);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/current-iteration")
    public ResponseEntity<Map<String, String>> getCurrentIteration() {
        log.debug("GET /api/v1/capacity/current-iteration");
        return ResponseEntity.ok(Map.of("name", capacityService.getActiveSprintName()));
    }

    /**
     * Export capacity planning as Excel (.xlsx) with two tables and AutoFilter.
     * Table 1: summary per sprint (PI, Iteration, Dates, Total EFT, 80%)
     * Table 2: detail per member (PI, Iteration, Type, Days, Name, Time, Time override, Days off, Effective days)
     */
    @GetMapping("/export")
    @RequiresFeature(FeatureCode.CSV_EXPORT)
    public ResponseEntity<byte[]> exportCapacity() {
        log.info("GET /api/v1/capacity/export");
        byte[] xlsx = csvExportService.exportCapacityToXlsx(
                capacityService.getCapacityPlanning(getOrgId()),
                capacityService.getSprintInfos()
        );
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"capacity-planning.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }

    private UUID getOrgId() {
        return getAuth().getOrganizationId();
    }

    private JwtAuthenticationToken getAuth() {
        return (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    }
}
