package com.company.sprintreporter.service.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when Jira API returns 401 Unauthorized.
 * Usually indicates invalid/expired token or incorrect credentials.
 */
public class JiraAuthenticationException extends JiraException {
    public JiraAuthenticationException(String token, String baseUrl) {
        super(
            "Jira authentication failed: your PAT/API token is invalid or expired. " +
            "Verify the token has not expired and the base URL is correct (e.g., https://jira.company.com). " +
            "Base URL: " + baseUrl,
            HttpStatus.BAD_GATEWAY
        );
    }

    public JiraAuthenticationException(String message) {
        super(message, HttpStatus.BAD_GATEWAY);
    }
}
