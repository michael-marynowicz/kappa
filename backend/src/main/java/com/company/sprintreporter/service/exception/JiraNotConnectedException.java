package com.company.sprintreporter.service.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an authenticated user tries to access Jira data
 * but has not yet connected their personal Jira credentials.
 * The frontend should redirect the user to the Jira credentials setup screen.
 */
public class JiraNotConnectedException extends JiraException {

    public JiraNotConnectedException() {
        super(
            "Your Jira account is not connected. " +
            "Please configure your Jira credentials before accessing dashboard data.",
            HttpStatus.PRECONDITION_REQUIRED
        );
    }
}
