package com.company.sprintreporter.service.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception for Jira API integration failures.
 * Subclasses provide specific error contexts.
 */
public abstract class JiraException extends RuntimeException {
    private final HttpStatus httpStatus;

    public JiraException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public JiraException(String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
