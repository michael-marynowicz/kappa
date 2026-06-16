error id: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/test/java/com/company/sprintreporter/infrastructure/jira/JiraIssueDomainMapperTest.java:JiraApiResponse/Fields#setSp10016#
file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/test/java/com/company/sprintreporter/infrastructure/jira/JiraIssueDomainMapperTest.java
empty definition using pc, found symbol in pc: JiraApiResponse/Fields#setSp10016#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 1355
uri: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/test/java/com/company/sprintreporter/infrastructure/jira/JiraIssueDomainMapperTest.java
text:
```scala
package com.company.sprintreporter.infrastructure.jira;

import com.company.sprintreporter.domain.model.SprintIssue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JiraIssueDomainMapper")
class JiraIssueDomainMapperTest {

    private final JiraIssueDomainMapper mapper = new JiraIssueDomainMapper();

    private JiraApiResponse.Issue buildJiraIssue(
            String key, String summary, String status,
            String assignee, String type, Double sp) {

        JiraApiResponse.Issue issue = new JiraApiResponse.Issue();
        issue.setId("10001");
        issue.setKey(key);

        JiraApiResponse.Fields fields = new JiraApiResponse.Fields();
        fields.setSummary(summary);

        JiraApiResponse.Status jiraStatus = new JiraApiResponse.Status();
        jiraStatus.setName(status);
        fields.setStatus(jiraStatus);

        if (assignee != null) {
            JiraApiResponse.Assignee jiraAssignee = new JiraApiResponse.Assignee();
            jiraAssignee.setDisplayName(assignee);
            fields.setAssignee(jiraAssignee);
        }

        JiraApiResponse.IssueType issueType = new JiraApiResponse.IssueType();
        issueType.setName(type);
        fields.setIssueType(issueType);

        fields.@@setSp10016(sp);
        issue.setFields(fields);

        return issue;
    }

    @Test
    @DisplayName("should correctly map all fields from Jira issue to domain model")
    void mapsAllFieldsCorrectly() {
        JiraApiResponse.Issue jiraIssue = buildJiraIssue(
                "SCRUM-42", "Build login page", "Done", "Alice Martin", "Story", 8.0);

        SprintIssue domain = mapper.toDomain(jiraIssue);

        assertThat(domain.getIssueKey()).isEqualTo("SCRUM-42");
        assertThat(domain.getSummary()).isEqualTo("Build login page");
        assertThat(domain.getStatus()).isEqualTo("Done");
        assertThat(domain.getAssignee()).isEqualTo("Alice Martin");
        assertThat(domain.getIssueType()).isEqualTo("Story");
        assertThat(domain.getTotalStoryPoints()).isEqualTo(8);
        assertThat(domain.getRemainingStoryPoints()).isNull(); // always null from Jira
    }

    @Test
    @DisplayName("should set assignee to 'Unassigned' when null")
    void setsUnassignedWhenAssigneeNull() {
        JiraApiResponse.Issue jiraIssue = buildJiraIssue(
                "SCRUM-1", "Task", "To Do", null, "Task", 3.0);

        SprintIssue domain = mapper.toDomain(jiraIssue);

        assertThat(domain.getAssignee()).isEqualTo("Unassigned");
    }

    @Test
    @DisplayName("should set null totalStoryPoints when Jira field is null")
    void setsNullSpWhenJiraFieldNull() {
        JiraApiResponse.Issue jiraIssue = buildJiraIssue(
                "SCRUM-1", "Task", "To Do", "Bob", "Task", null);

        SprintIssue domain = mapper.toDomain(jiraIssue);

        assertThat(domain.getTotalStoryPoints()).isNull();
    }

    @Test
    @DisplayName("should map a list of Jira issues")
    void mapsListOfIssues() {
        List<JiraApiResponse.Issue> jiraIssues = List.of(
                buildJiraIssue("SCRUM-1", "Story A", "Done", "Alice", "Story", 5.0),
                buildJiraIssue("SCRUM-2", "Story B", "In Progress", "Bob", "Story", 3.0)
        );

        List<SprintIssue> result = mapper.toDomainList(jiraIssues);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(SprintIssue::getIssueKey)
                .containsExactly("SCRUM-1", "SCRUM-2");
    }

    @Test
    @DisplayName("should return empty list when input is null")
    void returnsEmptyListForNullInput() {
        assertThat(mapper.toDomainList(null)).isEmpty();
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: JiraApiResponse/Fields#setSp10016#