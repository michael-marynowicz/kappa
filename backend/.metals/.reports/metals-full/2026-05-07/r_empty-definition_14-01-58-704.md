error id: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/infrastructure/jira/JiraApiResponse.java:com/fasterxml/jackson/annotation/JsonAnySetter#
file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/infrastructure/jira/JiraApiResponse.java
empty definition using pc, found symbol in pc: com/fasterxml/jackson/annotation/JsonAnySetter#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 97
uri: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/infrastructure/jira/JiraApiResponse.java
text:
```scala
package com.company.sprintreporter.infrastructure.jira;

import com.fasterxml.jackson.annotation.@@JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        // Story points — Jira Server instances use varying custom field IDs.
        // Declared as Object to survive any JSON structure (number, null, or nested object).
        @JsonProperty("customfield_10016")
        private Object sp10016raw;

        @JsonProperty("customfield_10028")
        private Object sp10028raw;

        @JsonProperty("customfield_10004")
        private Object sp10004raw;

        public Double getSp10016() { return toDouble(sp10016raw); }
        public Double getSp10028() { return toDouble(sp10028raw); }
        public Double getSp10004() { return toDouble(sp10004raw); }

        /** Captures every field not explicitly declared above (for SP field discovery). */
        private final Map<String, Object> extraFields = new HashMap<>();

        @JsonAnySetter
        public void setExtraField(String name, Object value) {
            extraFields.put(name, value);
        }

        public Map<String, Object> getExtraFields() {
            return extraFields;
        }

        /**
         * Returns the first non-null, non-sentinel story point value.
         * 2147483647 (Integer.MAX_VALUE) is a Jira sentinel for "unset integer field" — treated as null.
         */
        public Double getEffectiveStoryPoints() {
            if (isValidSp(getSp10016())) return getSp10016();
            if (isValidSp(getSp10028())) return getSp10028();
            if (isValidSp(getSp10004())) return getSp10004();
            return null;
        }

        private static boolean isValidSp(Double v) {
            return v != null && v != 2147483647.0;
        }

        /** Converts a raw JSON value (Number, Map, String) to Double, or null if unparseable. */
        private static Double toDouble(Object raw) {
            if (raw == null) return null;
            if (raw instanceof Number n) return n.doubleValue();
            // Some Jira instances return {"value": 5} for custom fields
            if (raw instanceof java.util.Map<?, ?> m && m.containsKey("value")) {
                Object v = m.get("value");
                if (v instanceof Number n) return n.doubleValue();
            }
            return null;
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

empty definition using pc, found symbol in pc: com/fasterxml/jackson/annotation/JsonAnySetter#