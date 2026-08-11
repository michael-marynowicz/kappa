package com.company.sprintreporter.infrastructure.jira;

import com.company.sprintreporter.domain.model.SprintInfo;
import com.company.sprintreporter.domain.model.SprintIssue;
import com.company.sprintreporter.domain.port.JiraIssueRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
                        .topic("Security")
                        .totalStoryPoints(8)
                        .remainingStoryPoints(null)
                        .addedAfterSprintStart(false)
                        .build(),
                SprintIssue.builder()
                        .issueKey("SCRUM-102")
                        .summary("Design sprint dashboard UI mockups")
                        .status("In Progress")
                        .assignee("Bob Chen")
                        .issueType("Story")
                        .topic("Frontend")
                        .totalStoryPoints(5)
                        .remainingStoryPoints(null)
                        .addedAfterSprintStart(false)
                        .build(),
                SprintIssue.builder()
                        .issueKey("SCRUM-103")
                        .summary("Fix pagination bug in user list endpoint")
                        .status("Done")
                        .assignee("Carol Smith")
                        .issueType("Bug")
                        .topic("Bug")
                        .totalStoryPoints(3)
                        .remainingStoryPoints(null)
                        .addedAfterSprintStart(false)
                        .build(),
                SprintIssue.builder()
                        .issueKey("SCRUM-104")
                        .summary("Write unit tests for payment service")
                        .status("To Do")
                        .assignee("Alice Martin")
                        .issueType("Task")
                        .topic("Quality")
                        .totalStoryPoints(5)
                        .remainingStoryPoints(null)
                        .addedAfterSprintStart(false)
                        .build(),
                SprintIssue.builder()
                        .issueKey("SCRUM-105")
                        .summary("Setup CI/CD pipeline for staging environment")
                        .status("Done")
                        .assignee("David Park")
                        .issueType("Task")
                        .topic("DevOps")
                        .totalStoryPoints(13)
                        .remainingStoryPoints(null)
                        .addedAfterSprintStart(false)
                        .build(),
                SprintIssue.builder()
                        .issueKey("SCRUM-106")
                        .summary("Refactor legacy notification service")
                        .status("In Progress")
                        .assignee("Bob Chen")
                        .issueType("Story")
                        .topic("Refactor")
                        .totalStoryPoints(8)
                        .remainingStoryPoints(null)
                        .addedAfterSprintStart(false)
                        .build(),
                SprintIssue.builder()
                        .issueKey("SCRUM-107")
                        .summary("Integrate Stripe payment gateway")
                        .status("To Do")
                        .assignee("Carol Smith")
                        .issueType("Story")
                        .topic("Backend")
                        .totalStoryPoints(13)
                        .remainingStoryPoints(null)
                        .addedAfterSprintStart(true)
                        .build(),
                SprintIssue.builder()
                        .issueKey("SCRUM-108")
                        .summary("Performance audit on product search API")
                        .status("Done")
                        .assignee("David Park")
                        .issueType("Task")
                        .topic("Quality")
                        .totalStoryPoints(5)
                        .remainingStoryPoints(null)
                        .addedAfterSprintStart(true)
                        .build()
        );
    }

    @Override
    public Map<String, List<SprintIssue>> fetchClosedSprintIssues() {
        log.debug("Returning mock closed sprint data");
        Map<String, List<SprintIssue>> result = new LinkedHashMap<>();

        result.put("Sprint 10", List.of(
                SprintIssue.builder().issueKey("SCRUM-80").summary("Login page").status("Done")
                        .assignee("Alice Martin").issueType("Story").topic("Security")
                        .totalStoryPoints(5).remainingStoryPoints(0).build(),
                SprintIssue.builder().issueKey("SCRUM-81").summary("API Gateway setup").status("Done")
                        .assignee("Bob Chen").issueType("Story").topic("Backend")
                        .totalStoryPoints(8).remainingStoryPoints(0).build(),
                SprintIssue.builder().issueKey("SCRUM-82").summary("DB migration").status("Done")
                        .assignee("Carol Smith").issueType("Task").topic("DevOps")
                        .totalStoryPoints(3).remainingStoryPoints(0).build(),
                SprintIssue.builder().issueKey("SCRUM-83").summary("User profile page").status("To Do")
                        .assignee("David Park").issueType("Story").topic("Frontend")
                        .totalStoryPoints(5).remainingStoryPoints(5).build()
        ));

        result.put("Sprint 11", List.of(
                SprintIssue.builder().issueKey("SCRUM-90").summary("Search feature").status("Done")
                        .assignee("Alice Martin").issueType("Story").topic("Backend")
                        .totalStoryPoints(8).remainingStoryPoints(0).build(),
                SprintIssue.builder().issueKey("SCRUM-91").summary("Email notifications").status("Done")
                        .assignee("Bob Chen").issueType("Story").topic("Backend")
                        .totalStoryPoints(5).remainingStoryPoints(0).build(),
                SprintIssue.builder().issueKey("SCRUM-92").summary("Dashboard charts").status("Done")
                        .assignee("Carol Smith").issueType("Story").topic("Frontend")
                        .totalStoryPoints(13).remainingStoryPoints(0).build(),
                SprintIssue.builder().issueKey("SCRUM-93").summary("Cache layer").status("In Progress")
                        .assignee("David Park").issueType("Story").topic("Backend")
                        .totalStoryPoints(8).remainingStoryPoints(3).build()
        ));

        result.put("Sprint 12", List.of(
                SprintIssue.builder().issueKey("SCRUM-95").summary("Payment flow").status("Done")
                        .assignee("Alice Martin").issueType("Story").topic("Backend")
                        .totalStoryPoints(13).remainingStoryPoints(0).build(),
                SprintIssue.builder().issueKey("SCRUM-96").summary("Mobile responsive").status("Done")
                        .assignee("Bob Chen").issueType("Story").topic("Frontend")
                        .totalStoryPoints(8).remainingStoryPoints(0).build(),
                SprintIssue.builder().issueKey("SCRUM-97").summary("Load testing").status("Done")
                        .assignee("Carol Smith").issueType("Task").topic("Quality")
                        .totalStoryPoints(5).remainingStoryPoints(0).build(),
                SprintIssue.builder().issueKey("SCRUM-98").summary("Audit logging").status("To Do")
                        .assignee("David Park").issueType("Story").topic("Security")
                        .totalStoryPoints(8).remainingStoryPoints(8).build()
        ));

        return result;
    }

    @Override
    public List<SprintInfo> fetchSprintInfos() {
        return List.of(
                SprintInfo.builder().name("Sprint 10")
                        .startDate(LocalDate.of(2026, 3, 30))
                        .endDate(LocalDate.of(2026, 4, 10)).build(),
                SprintInfo.builder().name("Sprint 11")
                        .startDate(LocalDate.of(2026, 4, 13))
                        .endDate(LocalDate.of(2026, 4, 24)).build(),
                SprintInfo.builder().name("Sprint 12")
                        .startDate(LocalDate.of(2026, 4, 27))
                        .endDate(LocalDate.of(2026, 5, 8)).build(),
                SprintInfo.builder().name("Current Sprint")
                        .startDate(LocalDate.of(2026, 5, 11))
                        .endDate(LocalDate.of(2026, 5, 22)).build()
        );
    }
}
