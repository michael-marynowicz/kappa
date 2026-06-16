error id: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/infrastructure/jira/JiraIssueDomainMapper.java:_empty_/log#
file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/infrastructure/jira/JiraIssueDomainMapper.java
empty definition using pc, found symbol in pc: _empty_/log#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 1633
uri: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/infrastructure/jira/JiraIssueDomainMapper.java
text:
```scala
package com.company.sprintreporter.infrastructure.jira;

import com.company.sprintreporter.domain.model.SprintIssue;
import org.springframework.stereotype.Component;

import java.util.List;
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
        if (jiraIssues == null) {
            return List.of();
        }
        return jiraIssues.stream()
                .filter(Objects::nonNull)
                .map(this::toDomain)
                .toList();
    }

    public SprintIssue toDomain(JiraApiResponse.Issue jiraIssue) {
        JiraApiResponse.Fields fields = jiraIssue.getFields();

        String assigneeName = (fields.getAssignee() != null)
                ? fields.getAssignee().getDisplayName()
                : "Unassigned";

        String statusName = (fields.getStatus() != null)
                ? fields.getStatus().getName()
                : "Unknown";

        String issueTypeName = (fields.getIssueType() != null)
                ? fields.getIssueType().getName()
                : "Unknown";

        Integer storyPoints = fields.getEffectiveStoryPoints() != null
                ? fields.getEffectiveStoryPoints().intValue()
                : null;

        if (storyPoints == null) {
            @@log.warn("Story points are null for issue {} — sp10016={}, sp10028={}, sp10004={}",
                    jiraIssue.getKey(), fields.getSp10016(), fields.getSp10028(), fields.getSp10004());
        }

        return SprintIssue.builder()
                .issueKey(jiraIssue.getKey())
                .summary(fields.getSummary())
                .status(statusName)
                .assignee(assigneeName)
                .issueType(issueTypeName)
                .totalStoryPoints(storyPoints)
                .remainingStoryPoints(null) // set later by user input
                .build();
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/log#