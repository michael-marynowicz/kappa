error id: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/controller/JiraConfigController.java:com/company/sprintreporter/service/jira/JiraConfigService#
file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/controller/JiraConfigController.java
empty definition using pc, found symbol in pc: com/company/sprintreporter/service/jira/JiraConfigService#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 709
uri: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/controller/JiraConfigController.java
text:
```scala
package com.company.sprintreporter.controller;

import com.company.sprintreporter.application.dto.jira.JiraConfigRequestDto;
import com.company.sprintreporter.application.dto.jira.JiraConfigResponseDto;
import com.company.sprintreporter.application.dto.jira.JiraTestConnectionResponseDto;
import com.company.sprintreporter.application.dto.jira.SaveCredentialsRequestDto;
import com.company.sprintreporter.application.dto.jira.SaveMyJiraCredentialsRequestDto;
import com.company.sprintreporter.config.jwt.JwtAuthenticationToken;
import com.company.sprintreporter.domain.entity.AppUser;
import com.company.sprintreporter.domain.entity.JiraConfiguration;
import com.company.sprintreporter.service.jira.@@JiraConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/jira")
@RequiredArgsConstructor
public class JiraConfigController {

    private final JiraConfigService jiraConfigService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    public ResponseEntity<JiraConfigResponseDto> getConfig() {
        var auth = getAuth();
        return jiraConfigService.findByOrganizationId(auth.getOrganizationId())
                .map(config -> ResponseEntity.ok(toDto(config)))
                .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JiraConfigResponseDto> saveConfig(@Valid @RequestBody JiraConfigRequestDto request) {
        var auth = getAuth();
        JiraConfiguration config = jiraConfigService.createOrUpdate(
                auth.getOrganizationId(),
                request.getBaseUrl(), request.getAuthType(),
                request.getUserEmail(), request.getToken(),
                request.getProjectKey(), request.getBoardId());
        return ResponseEntity.ok(toDto(config));
    }

    /**
     * Step 1 of onboarding: save Jira credentials only (no projectKey / boardId).
     * projectKey and boardId are set per-dashboard via POST /api/v1/jira/dashboards.
     */
    @PutMapping("/credentials")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JiraConfigResponseDto> saveCredentials(@Valid @RequestBody SaveCredentialsRequestDto request) {
        var auth = getAuth();
        JiraConfiguration config = jiraConfigService.saveCredentials(
                auth.getOrganizationId(),
                request.getBaseUrl(), request.getAuthType(),
                request.getUserEmail(), request.getToken());
        return ResponseEntity.ok(toDto(config));
    }

    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JiraTestConnectionResponseDto> testConnection() {
        var auth = getAuth();
        boolean success = jiraConfigService.testConnection(auth.getOrganizationId());
        return ResponseEntity.ok(JiraTestConnectionResponseDto.builder()
                .success(success)
            .message(success
                ? "Connection successful"
                : "Connection failed")
                .build());
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> triggerSync() {
        var auth = getAuth();
        jiraConfigService.triggerSync(auth.getOrganizationId());
        return ResponseEntity.accepted().build();
    }

    /**
     * Each user saves their own Jira username + password.
     * Credentials are tested against the org's Jira baseUrl before being persisted.
     * Returns { connected: true, username: "..." }.
     */
    @PutMapping("/my-credentials")
    public ResponseEntity<Map<String, Object>> saveMyCredentials(@Valid @RequestBody SaveMyJiraCredentialsRequestDto request) {
        var auth = getAuth();
        AppUser user = jiraConfigService.saveMyCredentials(
                auth.getUserId(), auth.getOrganizationId(),
                request.getUsername(), request.getPassword());
        return ResponseEntity.ok(Map.of(
                "connected", user.getJiraConnected(),
                "username", user.getJiraUsername()));
    }

    /**
     * Returns the current user's Jira connection status.
     */
    @GetMapping("/my-credentials")
    public ResponseEntity<Map<String, Object>> getMyCredentials() {
        var auth = getAuth();
        return jiraConfigService.findUserWithJiraCredentials(auth.getUserId())
                .map(u -> ResponseEntity.ok(Map.<String, Object>of(
                        "connected", true,
                        "username", u.getJiraUsername())))
                .orElse(ResponseEntity.ok(Map.of("connected", false)));
    }

    private JiraConfigResponseDto toDto(JiraConfiguration config) {
        return JiraConfigResponseDto.builder()
                .id(config.getId())
                .baseUrl(config.getBaseUrl())
                .authType(config.getAuthType())
                .userEmail(config.getUserEmail())
                .projectKey(config.getProjectKey())
                .boardId(config.getBoardId())
                .active(config.getActive())
                .lastSyncAt(config.getLastSyncAt())
                .build();
    }

    private JwtAuthenticationToken getAuth() {
        return (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: com/company/sprintreporter/service/jira/JiraConfigService#