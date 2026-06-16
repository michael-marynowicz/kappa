package com.company.sprintreporter.infrastructure.jira;

import com.company.sprintreporter.domain.model.SprintIssue;
import com.company.sprintreporter.domain.port.JiraIssueRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mock implementation of JiraIssueRepository for development and demo mode.
 * Returns realistic sprint data matching the structure of a real Jira sprint.
 *
 * This is activated when jira.mock-mode=true (default for local dev).
 * In production, the JiraIssueRepositoryImpl takes precedence (@Primary).
 */
@Slf4j
@Component
public class MockJiraIssueRepository implements JiraIssueRepository {

    @Override
    public List<SprintIssue> fetchSprintIssues() {
        log.debug("Returning mock sprint issues");
        return List.of(
                SprintIssue.builder()
                        .issueKey("SCRUM-101")
                        .summary("Implement user authentication with OAuth2")
                        .status("Done")
                        .assignee("Alice Martin")
                        .issueType("Story")
                        .totalStoryPoints(8)
                        .remainingStoryPoints(null)
                        .build(),
                SprintIssue.builder()
                        .issueKey("SCRUM-102")
                        .summary("Design sprint dashboard UI mockups")
                        .status("In Progress")
                        .assignee("Bob Chen")
                        .issueType("Story")
                        .totalStoryPoints(5)
                        .remainingStoryPoints(null)
                        .build(),
                SprintIssue.builder()
                        .issueKey("SCRUM-103")
                        .summary("Fix pagination bug in user list endpoint")
                        .status("In Progress")
                        .assignee("Carol Smith")
                        .issueType("Bug")
                        .totalStoryPoints(3)
                        .remainingStoryPoints(null)
                        .build(),
                SprintIssue.builder()
                        .issueKey("SCRUM-104")
                        .summary("Write unit tests for payment service")
                        .status("To Do")
                        .assignee("Alice Martin")
                        .issueType("Task")
                        .totalStoryPoints(5)
                        .remainingStoryPoints(null)
                        .build(),
                SprintIssue.builder()
                        .issueKey("SCRUM-105")
                        .summary("Setup CI/CD pipeline for staging environment")
                        .status("Done")
                        .assignee("David Park")
                        .issueType("Task")
                        .totalStoryPoints(13)
                        .remainingStoryPoints(null)
                        .build(),
                SprintIssue.builder()
                        .issueKey("SCRUM-106")
                        .summary("Refactor legacy notification service")
                        .status("In Progress")
                        .assignee("Bob Chen")
                        .issueType("Story")
                        .totalStoryPoints(8)
                        .remainingStoryPoints(null)
                        .build(),
                SprintIssue.builder()
                        .issueKey("SCRUM-107")
                        .summary("Integrate Stripe payment gateway")
                        .status("To Do")
                        .assignee("Carol Smith")
                        .issueType("Story")
                        .totalStoryPoints(13)
                        .remainingStoryPoints(null)
                        .build(),
                SprintIssue.builder()
                        .issueKey("SCRUM-108")
                        .summary("Performance audit on product search API")
                        .status("In Review")
                        .assignee("David Park")
                        .issueType("Task")
                        .totalStoryPoints(5)
                        .remainingStoryPoints(null)
                        .build()
        );
    }
}
