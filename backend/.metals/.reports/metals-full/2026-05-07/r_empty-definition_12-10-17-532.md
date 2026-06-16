error id: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/infrastructure/jira/JiraApiResponse.java:java/util/List#
file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/infrastructure/jira/JiraApiResponse.java
empty definition using pc, found symbol in pc: java/util/List#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 211
uri: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/infrastructure/jira/JiraApiResponse.java
text:
```scala
package com.company.sprintreporter.infrastructure.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.@@List;

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
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fields {
        private String summary;
        private Status status;
        private Assignee assignee;

        @JsonProperty("issuetype")
        private IssueType issueType;

        /**
         * Story points — Jira uses a custom field.
         * customfield_10016 is the standard Jira Software field for story points.
         * Some Jira Server instances use customfield_10028 or story_points instead.
         */
        @JsonProperty("customfield_10016")
        private Double storyPoints;

        @JsonProperty("customfield_10028")
        private Double storyPointsAlt;

        /** Convenience getter: returns whichever story point field is populated. */
        public Double getEffectiveStoryPoints() {
            if (storyPoints != null) return storyPoints;
            return storyPointsAlt;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        private String name;
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

    /**
     * Response from GET /rest/agile/1.0/board/{boardId}/sprint
     * Returns paginated list of sprints for the board.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SprintList {
        private List<Sprint> values;
        private boolean isLast;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sprint {
        private int id;
        private String name;
        private String state;
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: java/util/List#