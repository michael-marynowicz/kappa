package com.company.sprintreporter.infrastructure.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Raw Jira API response models.
 * These live ONLY in the infrastructure layer.
 * The domain model is completely isolated from these structures.
 *
 * Example Jira response:
 * {
 *   "issues": [
 *     {
 *       "id": "10001",
 *       "key": "SCRUM-42",
 *       "fields": {
 *         "summary": "Implement login page",
 *         "status": { "name": "In Progress" },
 *         "assignee": { "displayName": "Alice Martin" },
 *         "issuetype": { "name": "Story" },
 *         "story_points": 5,
 *         "customfield_10016": 5
 *       }
 *     }
 *   ],
 *   "total": 1
 * }
 */
public class JiraApiResponse {

    private JiraApiResponse() {}

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchResult {
        private List<Issue> issues;
        private int total;
        private int maxResults;
        private int startAt;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Issue {
        private String id;
        private String key;
        private Fields fields;
        private String epic;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fields {
        private String summary;
        private Status status;
        private Assignee assignee;

        @JsonProperty("issuetype")
        private IssueType issueType;

        @JsonProperty("customfield_10002")
        private Double storyPoints;

        @JsonProperty("customfield_20896")
        private Double remainingStoryPoints;

        @JsonProperty("customfield_16701")
        private Double doneStoryPoints;

        private List<String> labels;

        private ParentIssue parent;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParentIssue {
        private String key;
        private Fields fields;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        private String name;
        private StatusCategory statusCategory;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatusCategory {
        private String key;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Assignee {
        private String displayName;
        private String emailAddress;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IssueType {
        private String name;
    }
}
