error id: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/infrastructure/jira/JiraIssueRepositoryImpl.java:_empty_/log#
file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/infrastructure/jira/JiraIssueRepositoryImpl.java
empty definition using pc, found symbol in pc: _empty_/log#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 1249
uri: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/infrastructure/jira/JiraIssueRepositoryImpl.java
text:
```scala
package com.company.sprintreporter.infrastructure.jira;

import com.company.sprintreporter.domain.model.SprintIssue;
import com.company.sprintreporter.domain.port.JiraIssueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Infrastructure implementation of JiraIssueRepository.
 * Communicates with Jira Server/Data Center REST API v2.
 *
 * Uses JQL "sprint in openSprints()" to fetch active sprint issues directly,
 * avoiding the Agile board API which may not be available on all instances.
 *
 * In mock mode (jira.mock-mode=true), delegates to MockJiraIssueRepository.
 */
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class JiraIssueRepositoryImpl implements JiraIssueRepository {

    private final JiraProperties jiraProperties;
    private final JiraIssueDomainMapper mapper;
    private final MockJiraIssueRepository mockRepository;

    @Override
    public List<SprintIssue> fetchSprintIssues() {
        if (jiraProperties.isMockMode()) {
            @@log.info("Mock mode active — returning mock Jira data");
            return mockRepository.fetchSprintIssues();
        }

        return fetchFromJiraApi();
    }

    private List<SprintIssue> fetchFromJiraApi() {
        WebClient client = WebClient.builder()
                .baseUrl(jiraProperties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + jiraProperties.getPat())
                .defaultHeader("Accept", "application/json")
                .build();

        // Step 1: resolve the active sprint for the configured board
        int sprintId = resolveActiveSprintId(client);

        // Step 2: fetch issues in that sprint, optionally filtered by assignee
        log.info("Fetching sprint issues from Jira Agile API for sprint {}", sprintId);

        String jqlFilter = buildJqlFilter();
        log.debug("JQL filter: {}", jqlFilter.isEmpty() ? "(none)" : jqlFilter);

        JiraApiResponse.SearchResult result = client.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/rest/agile/1.0/sprint/{sprintId}/issue")
                            // customfield_10016/10028/10004 are the most common story point field IDs on Jira Server
                            .queryParam("fields", "summary,status,assignee,issuetype,customfield_10016,customfield_10028,customfield_10004")
                            .queryParam("maxResults", 200);
                    if (!jqlFilter.isEmpty()) {
                        builder = builder.queryParam("jql", jqlFilter);
                    }
                    return builder.build(sprintId);
                })
                .retrieve()
                .bodyToMono(JiraApiResponse.SearchResult.class)
                .block();

        if (result == null || result.getIssues() == null) {
            log.warn("Jira API returned empty result for sprint {}", sprintId);
            return List.of();
        }

        log.info("Received {} issues from Jira API", result.getIssues().size());
        return mapper.toDomainList(result.getIssues());
    }

    private int resolveActiveSprintId(WebClient client) {
        log.info("Looking up active sprint for board {}", jiraProperties.getBoardId());

        JiraApiResponse.SprintList sprintList = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/agile/1.0/board/{boardId}/sprint")
                        .queryParam("state", "active")
                        .build(jiraProperties.getBoardId()))
                .retrieve()
                .bodyToMono(JiraApiResponse.SprintList.class)
                .block();

        if (sprintList == null || sprintList.getValues() == null || sprintList.getValues().isEmpty()) {
            throw new IllegalStateException(
                    "No active sprint found for board " + jiraProperties.getBoardId());
        }

        JiraApiResponse.Sprint activeSprint = sprintList.getValues().get(0);
        log.info("Active sprint found: {} (id={})", activeSprint.getName(), activeSprint.getId());
        return activeSprint.getId();
    }

    private String buildJqlFilter() {
        List<String> conditions = new java.util.ArrayList<>();

        // Always exclude sub-tasks — we only want Stories, Bugs, Tasks
        conditions.add("issuetype not in subTaskIssueTypes()");

        // Optionally restrict to specific team members
        List<String> members = jiraProperties.getTeamMembers();
        if (members != null && !members.isEmpty()) {
            String assigneeList = members.stream()
                    .map(u -> "\"" + u + "\"")
                    .collect(Collectors.joining(", "));
            log.info("Filtering sprint issues for {} team member(s)", members.size());
            conditions.add("assignee in (" + assigneeList + ")");
        }

        return String.join(" AND ", conditions);
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/log#