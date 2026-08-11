package com.company.sprintreporter.service.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a user requests Jira data but no active dashboard is configured.
 * The frontend should redirect the user to the dashboard selection screen.
 */
public class NoDashboardSelectedException extends JiraException {

    public NoDashboardSelectedException() {
        super(
            "No active dashboard configured. Please select a Jira board before accessing data.",
            HttpStatus.PRECONDITION_REQUIRED
        );
    }
}
