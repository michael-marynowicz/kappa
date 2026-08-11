package com.company.sprintreporter.controller;

import com.company.sprintreporter.config.AtlassianOAuth2Properties;
import com.company.sprintreporter.config.jwt.JwtAuthenticationToken;
import com.company.sprintreporter.service.jira.JiraOAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/jira/oauth")
@RequiredArgsConstructor
@Slf4j
public class JiraOAuthController {

    private final JiraOAuthService jiraOAuthService;
    private final AtlassianOAuth2Properties props;

    /**
     * Returns the Atlassian authorization URL that the admin's browser should be redirected to.
     * Only accessible to org admins.
     */
    @GetMapping("/connect")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> connect() {
        var auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        String authUrl = jiraOAuthService.buildAuthorizationUrl(auth.getOrganizationId());
        return ResponseEntity.ok(Map.of("authUrl", authUrl));
    }

    /**
     * OAuth 2.0 callback endpoint — called by Atlassian after user authorizes access.
     * Not protected by JWT (browser redirect, no Authorization header).
     * Redirects the browser to the configured frontend URL after completion.
     */
    @GetMapping("/callback")
    public void callback(@RequestParam String code,
                         @RequestParam String state,
                         HttpServletResponse response) throws IOException {
        try {
            jiraOAuthService.handleCallback(code, state);
            response.sendRedirect(props.getFrontendSuccessUrl());
        } catch (Exception e) {
            log.error("Atlassian OAuth callback failed: {}", e.getMessage(), e);
            String errorParam = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect(props.getFrontendErrorUrl() + "&message=" + errorParam);
        }
    }

    /**
     * Removes OAuth tokens and marks the Jira config inactive.
     * Only accessible to org admins.
     */
    @DeleteMapping("/disconnect")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> disconnect() {
        var auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        jiraOAuthService.disconnect(auth.getOrganizationId());
        return ResponseEntity.noContent().build();
    }
}
