package com.company.sprintreporter.service.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown for generic Jira API errors (4xx/5xx responses).
 */
public class JiraApiException extends JiraException {
    private final int statusCode;

    public JiraApiException(int statusCode, String statusText, String endpoint) {
        super(
            "Jira API error: HTTP " + statusCode + " (" + statusText + ") from " + endpoint + ". " +
            "Verify your Jira base URL is correct and the endpoint exists.",
            mapStatusCode(statusCode)
        );
        this.statusCode = statusCode;
    }

    public JiraApiException(String message, HttpStatus status) {
        super(message, status);
        this.statusCode = status.value();
    }

    private static HttpStatus mapStatusCode(int code) {
        return switch (code) {
            case 400 -> HttpStatus.BAD_REQUEST;
            case 404 -> HttpStatus.NOT_FOUND;
            case 429 -> HttpStatus.TOO_MANY_REQUESTS;
            case 500, 502, 503, 504 -> HttpStatus.BAD_GATEWAY;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    public int getStatusCode() {
        return statusCode;
    }
}
