package com.company.sprintreporter.controller;

import com.company.sprintreporter.application.dto.SprintIssueResponseDto;
import com.company.sprintreporter.application.dto.UpdateRemainingSpRequestDto;
import com.company.sprintreporter.application.mapper.SprintIssueMapper;
import com.company.sprintreporter.domain.model.SprintIssue;
import com.company.sprintreporter.service.SprintIssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        List<SprintIssue> issues = sprintIssueService.getSprintIssues();
        return ResponseEntity.ok(mapper.toResponseDtoList(issues));
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
                request.getIssueKey(),
                request.getRemainingStoryPoints()
        );

        return ResponseEntity.ok(mapper.toResponseDto(updated));
    }
}
