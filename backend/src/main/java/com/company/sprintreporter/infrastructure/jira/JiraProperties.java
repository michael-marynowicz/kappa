package com.company.sprintreporter.infrastructure.jira;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
    private String apiToken;
    private String userEmail;
    private String projectKey;
    private String sprintId;
    private String boardId;

    /**
     * Personal Access Token for Jira Server/Data Center (Jira 8.14+).
     * When set, used as "Authorization: Bearer <pat>" — no expiry, no cookie refresh needed.
     * Generate one at: Jira → Profile → Personal Access Tokens.
     */
    private String pat;

    /**
     * When true, all Jira API calls are replaced by mock data.
     * Useful for local development or demo environments.
     */
    private boolean mockMode = true;
}
