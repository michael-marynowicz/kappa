package com.company.sprintreporter.controller;

import com.company.sprintreporter.application.dto.jira.BoardDiscoveryRequestDto;
import com.company.sprintreporter.application.dto.jira.BoardDto;
import com.company.sprintreporter.application.dto.jira.DashboardDto;
import com.company.sprintreporter.config.jwt.JwtAuthenticationToken;
import com.company.sprintreporter.domain.entity.enums.UserRole;
import com.company.sprintreporter.service.jira.BoardDiscoveryService;
import com.company.sprintreporter.service.jira.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jira")
@RequiredArgsConstructor
public class DashboardController {

    private final BoardDiscoveryService boardDiscoveryService;
    private final DashboardService dashboardService;

    /**
     * Discover boards available on the given Jira instance with the provided credentials.
     * Used by the setup wizard before saving any config.
     */
    @PostMapping("/boards/discover")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BoardDto>> discoverBoards(@RequestBody BoardDiscoveryRequestDto request) {
        return ResponseEntity.ok(boardDiscoveryService.discoverBoards(request));
    }

    /**
     * List all dashboards (boards) configured for the org.
     */
    @GetMapping("/dashboards")
    public ResponseEntity<List<DashboardDto>> listDashboards() {
        JwtAuthenticationToken auth = getAuth();
        return ResponseEntity.ok(dashboardService.listDashboards(auth.getOrganizationId(), auth.getUserId()));
    }

    /**
     * Add a new dashboard (board) to the org.
     * Body: { "name": "ROC Sprint Board", "boardId": 30025, "projectKey": "ROC" }
     */
    @PostMapping("/dashboards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardDto> addDashboard(@RequestBody Map<String, Object> body) {
        JwtAuthenticationToken auth = getAuth();
        String name = (String) body.get("name");
        Integer boardId = (Integer) body.get("boardId");
        String projectKey = (String) body.get("projectKey");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dashboardService.addDashboard(auth.getOrganizationId(), auth.getUserId(), name, boardId, projectKey));
    }

    /**
     * Switch the active dashboard for the org.
     */
    @PutMapping("/dashboards/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardDto> activateDashboard(@PathVariable UUID id) {
        return ResponseEntity.ok(dashboardService.activate(getOrgId(), id));
    }

    /**
     * Remove a dashboard from the org.
     */
    @DeleteMapping("/dashboards/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDashboard(@PathVariable UUID id) {
        dashboardService.deleteDashboard(getOrgId(), id);
        return ResponseEntity.noContent().build();
    }

    private JwtAuthenticationToken getAuth() {
        return (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    }

    private UUID getOrgId() {
        return getAuth().getOrganizationId();
    }
}
