package com.company.sprintreporter.controller;

import com.company.sprintreporter.application.dto.EpicSummaryDto;
import com.company.sprintreporter.application.dto.SprintIssueResponseDto;
import com.company.sprintreporter.application.dto.UpdateRemainingSpRequestDto;
import com.company.sprintreporter.application.mapper.SprintIssueMapper;
import com.company.sprintreporter.config.feature.FeatureCode;
import com.company.sprintreporter.config.feature.RequiresFeature;
import com.company.sprintreporter.config.jwt.JwtAuthenticationToken;
import com.company.sprintreporter.domain.model.SprintIssue;
import com.company.sprintreporter.service.SprintIssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller: Issues resource.
 *
 * Responsibilities (ONLY):
 * - Map HTTP requests to service calls
 * - Map domain models to response DTOs
 * - Return appropriate HTTP status codes
 *
 * Must NOT contain any business logic.
 * Must NOT call the Jira API directly.
 * Must NOT manipulate domain objects directly.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/issues")
@RequiredArgsConstructor
@RequiresFeature(FeatureCode.SPRINT_TRACKING)
public class IssueController {

    private final SprintIssueService sprintIssueService;
    private final SprintIssueMapper mapper;

    /**
     * GET /api/v1/issues
     * Returns all sprint issues with computed KPIs.
     */
    @GetMapping
    public ResponseEntity<List<SprintIssueResponseDto>> getSprintIssues() {
        log.debug("GET /api/v1/issues");
        List<SprintIssue> issues = sprintIssueService.getSprintIssues(getOrgId());
        return ResponseEntity.ok(mapper.toResponseDtoList(issues));
    }

    /**
     * GET /api/v1/issues/grouped
     * Returns sprint issues grouped by epic with aggregated KPIs.
     */
    @GetMapping("/grouped")
    public ResponseEntity<List<EpicSummaryDto>> getIssuesGroupedByEpic() {
        log.debug("GET /api/v1/issues/grouped");
        return ResponseEntity.ok(sprintIssueService.getIssuesGroupedByEpic(getOrgId()));
    }

    /**
     * POST /api/v1/issues/update
     * Updates remaining story points for a specific issue.
     */
    @PostMapping("/update")
    public ResponseEntity<SprintIssueResponseDto> updateRemainingStoryPoints(
            @Valid @RequestBody UpdateRemainingSpRequestDto request) {

        log.debug("POST /api/v1/issues/update - issueKey={}, remainingSP={}",
                request.getIssueKey(), request.getRemainingStoryPoints());

        SprintIssue updated = sprintIssueService.updateRemainingStoryPoints(
                getOrgId(),
                request.getIssueKey(),
                request.getRemainingStoryPoints()
        );

        return ResponseEntity.ok(mapper.toResponseDto(updated));
    }

    private UUID getOrgId() {
        return ((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getOrganizationId();
    }
}
