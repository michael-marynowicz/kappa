package com.company.sprintreporter.service.exception;

/**
 * Thrown when an issue key cannot be resolved within the current sprint.
 * Domain-level exception — not tied to HTTP or any transport concern.
 */
public class IssueNotFoundException extends RuntimeException {

    public IssueNotFoundException(String message) {
        super(message);
    }
}
