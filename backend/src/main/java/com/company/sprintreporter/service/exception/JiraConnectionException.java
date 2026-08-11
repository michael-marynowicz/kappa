package com.company.sprintreporter.service.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when unable to reach Jira (network error, timeout, etc).
 */
public class JiraConnectionException extends JiraException {
    public JiraConnectionException(String baseUrl, Throwable cause) {
        super(
            "Cannot reach Jira at " + baseUrl + ". Check your network connectivity and that the base URL is correct.",
            HttpStatus.SERVICE_UNAVAILABLE,
            cause
        );
    }

    public JiraConnectionException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
