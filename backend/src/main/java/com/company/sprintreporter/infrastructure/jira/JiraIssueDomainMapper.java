package com.company.sprintreporter.infrastructure.jira;

import com.company.sprintreporter.domain.model.SprintIssue;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Anti-corruption layer: maps raw Jira API structures to clean domain models.
 * This is the boundary where Jira's messy, nullable, custom-field-heavy structure
 * gets transformed into a clean, type-safe domain model.
 *
 * This class must NOT leak into the service or controller layers.
 */
@Component
public class JiraIssueDomainMapper {

    public List<SprintIssue> toDomainList(List<JiraApiResponse.Issue> jiraIssues) {
        return toDomainList(jiraIssues, Map.of(), Map.of());
    }

    public List<SprintIssue> toDomainList(List<JiraApiResponse.Issue> jiraIssues, Map<String, String> epicNames) {
        return toDomainList(jiraIssues, epicNames, Map.of());
    }

    public List<SprintIssue> toDomainList(List<JiraApiResponse.Issue> jiraIssues, Map<String, String> epicNames, Map<String, String> issueToEpicKey) {
        if (jiraIssues == null) {
            return List.of();
        }
        return jiraIssues.stream()
                .filter(Objects::nonNull)
                .map(issue -> toDomain(issue, epicNames, issueToEpicKey))
                .toList();
    }

    public SprintIssue toDomain(JiraApiResponse.Issue jiraIssue) {
        return toDomain(jiraIssue, Map.of(), Map.of());
    }

    public SprintIssue toDomain(JiraApiResponse.Issue jiraIssue, Map<String, String> epicNames) {
        return toDomain(jiraIssue, epicNames, Map.of());
    }

    public SprintIssue toDomain(JiraApiResponse.Issue jiraIssue, Map<String, String> epicNames, Map<String, String> issueToEpicKey) {
        JiraApiResponse.Fields fields = jiraIssue.getFields();

        String assigneeName = (fields.getAssignee() != null)
                ? fields.getAssignee().getDisplayName()
                : "Unassigned";

        String statusName = (fields.getStatus() != null)
                ? fields.getStatus().getName()
                : "Unknown";

        String statusCategoryKey = (fields.getStatus() != null && fields.getStatus().getStatusCategory() != null)
                ? fields.getStatus().getStatusCategory().getKey()
                : null;

        String issueTypeName = (fields.getIssueType() != null)
                ? fields.getIssueType().getName()
                : "Unknown";

        Integer storyPoints = fields.getStoryPoints() != null
                ? fields.getStoryPoints().intValue()
                : null;

        Integer remainingStoryPoints = fields.getRemainingStoryPoints() != null
                ? fields.getRemainingStoryPoints().intValue()
                : null;

        // Priority: Greenhopper epic → parent summary → first label → "Other"
        String topic = resolveTopicFromEpicOrParent(jiraIssue, fields, epicNames, issueToEpicKey);

        return SprintIssue.builder()
                .issueKey(jiraIssue.getKey())
                .summary(fields.getSummary())
                .status(statusName)
                .statusCategoryKey(statusCategoryKey)
                .assignee(assigneeName)
                .issueType(issueTypeName)
                .topic(topic)
                .totalStoryPoints(storyPoints)
                .remainingStoryPoints(remainingStoryPoints)
                .build();
    }

    private String resolveTopicFromEpicOrParent(JiraApiResponse.Issue issue, JiraApiResponse.Fields fields,
                                                Map<String, String> epicNames, Map<String, String> issueToEpicKey) {
        // 1. Try Greenhopper mapping: issueKey → epicKey → resolved epic name
        String epicKey = issueToEpicKey.get(issue.getKey());
        if (epicKey != null && epicNames.containsKey(epicKey)) {
            return epicNames.get(epicKey);
        }

        // 2. Fallback: try epic field from Agile API (may be null on Jira Server)
        if (issue.getEpic() != null && epicNames.containsKey(issue.getEpic())) {
            return epicNames.get(issue.getEpic());
        }

        // 3. Try Parent Link summary (direct parent issue)
        if (fields.getParent() != null
                && fields.getParent().getFields() != null
                && fields.getParent().getFields().getSummary() != null
                && !fields.getParent().getFields().getSummary().isBlank()) {
            return fields.getParent().getFields().getSummary();
        }

        // 4. Fallback to first label
        if (fields.getLabels() != null && !fields.getLabels().isEmpty()) {
            return fields.getLabels().get(0);
        }

        return "Other";
    }
}
