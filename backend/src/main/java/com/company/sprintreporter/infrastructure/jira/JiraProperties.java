package com.company.sprintreporter.infrastructure.jira;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Typed configuration for Jira connectivity.
 * Bound from application.yml under the "jira" prefix.
 * Using a @ConfigurationProperties class instead of @Value for testability and clarity.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "jira")
public class JiraProperties {

    private String baseUrl;
    /** Personal Access Token for Jira Server / Data Center (Bearer auth). */
    private String pat;
    private String userEmail;
    private String projectKey;
    /** Jira Software board ID — used to discover the active sprint automatically. */
    private int boardId;

    /**
     * Jira usernames of team members to include in sprint reports.
     * When non-empty, only issues assigned to these users are fetched.
     * Leave empty to include all sprint issues regardless of assignee.
     */
    private List<String> teamMembers = List.of();

    /**
     * When true, all Jira API calls are replaced by mock data.
     * Useful for local development or demo environments.
     */
    private boolean mockMode = true;
}
