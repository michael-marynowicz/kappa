error id: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/test/java/com/company/sprintreporter/service/CsvExportServiceTest.java:com/company/sprintreporter/service/CsvExportServiceTest#assertThat#describedAs#
file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/test/java/com/company/sprintreporter/service/CsvExportServiceTest.java
empty definition using pc, found symbol in pc: com/company/sprintreporter/service/CsvExportServiceTest#assertThat#describedAs#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 3159
uri: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/test/java/com/company/sprintreporter/service/CsvExportServiceTest.java
text:
```scala
package com.company.sprintreporter.service;

import com.company.sprintreporter.domain.model.SprintIssue;
import com.company.sprintreporter.domain.model.SprintMetrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CsvExportService")
class CsvExportServiceTest {

    private final CsvExportService service = new CsvExportService();

    private SprintMetrics emptyMetrics() {
        return SprintMetrics.compute(List.of());
    }

    @Test
    @DisplayName("should include KPI summary section header")
    void includesSummaryHeader() {
        String csv = service.exportToCsv(List.of(), emptyMetrics());
        assertThat(csv).contains("SPRINT KPI SUMMARY");
    }

    @Test
    @DisplayName("should include all metrics columns")
    void includesAllMetricsColumns() {
        String csv = service.exportToCsv(List.of(), emptyMetrics());
        assertThat(csv).contains("Committed SP");
        assertThat(csv).contains("Predictability (%)");
        assertThat(csv).contains("Sprint Health Score (0-100)");
        assertThat(csv).contains("Blocked Ratio (%)");
        assertThat(csv).contains("Team Efficiency (%)");
    }

    @Test
    @DisplayName("should include issue detail section")
    void includesIssueDetailSection() {
        String csv = service.exportToCsv(List.of(), emptyMetrics());
        assertThat(csv).contains("ISSUE DETAIL");
        assertThat(csv).contains("Issue Key");
        assertThat(csv).contains("Done SP");
    }

    @Test
    @DisplayName("should include one row per issue")
    void includesOneRowPerIssue() {
        SprintIssue issue = SprintIssue.builder()
                .issueKey("SCRUM-1").summary("Build login page")
                .status("Done").assignee("Alice").issueType("Story")
                .totalStoryPoints(8).remainingStoryPoints(0)
                .build();

        List<SprintIssue> issues = List.of(issue);
        String csv = service.exportToCsv(issues, SprintMetrics.compute(issues));

        assertThat(csv).contains("SCRUM-1");
        assertThat(csv).contains("Build login page");
        assertThat(csv).contains("Alice");
    }

    @Test
    @DisplayName("should compute correct metrics for issues")
    void computesCorrectMetrics() {
        SprintIssue done = SprintIssue.builder()
                .issueKey("SCRUM-1").summary("Done task")
                .status("Done").issueType("Story")
                .totalStoryPoints(5).remainingStoryPoints(0).build();
        SprintIssue inProg = SprintIssue.builder()
                .issueKey("SCRUM-2").summary("In progress")
                .status("In Progress").issueType("Bug")
                .totalStoryPoints(3).remainingStoryPoints(2).build();

        List<SprintIssue> issues = List.of(done, inProg);
        SprintMetrics metrics = SprintMetrics.compute(issues);
        String csv = service.exportToCsv(issues, metrics);

        // committedSP = 8, completedSP = 5
        assertThat(csv).contains("8");  // committed
        assertThat(csv).contains("5");  // completed
        assertThat(csv).@@describedAs("Sprint success should be No because 5 < 8").contains("No");
    }

    @Test
    @DisplayName("should handle null story points gracefully — no NPE")
    void handlesNullStoryPointsGracefully() {
        SprintIssue issue = SprintIssue.builder()
                .issueKey("SCRUM-2").summary("Unestimated")
                .status("To Do").issueType("Task")
                .totalStoryPoints(null).remainingStoryPoints(null).build();

        assertThatCode(() -> service.exportToCsv(List.of(issue), SprintMetrics.compute(List.of(issue))))
                .doesNotThrowAnyException();
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: com/company/sprintreporter/service/CsvExportServiceTest#assertThat#describedAs#