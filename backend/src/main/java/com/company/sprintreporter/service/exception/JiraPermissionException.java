package com.company.sprintreporter.service.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when Jira API returns 403 Forbidden.
 * Usually indicates the token/user lacks required permissions on the project or board.
 */
public class JiraPermissionException extends JiraException {
    public JiraPermissionException(String projectKey, String boardId) {
        super(
            "Jira permission denied: your token does not have access to project '" + projectKey + "' " +
            "or board '" + boardId + "'. Verify the token has read access to the project and sprint board.",
            HttpStatus.FORBIDDEN
        );
    }

    public JiraPermissionException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
