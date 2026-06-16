package com.company.sprintreporter.service;

import com.company.sprintreporter.domain.model.SprintIssue;
import com.company.sprintreporter.domain.model.SprintMetrics;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.List;
import java.util.Objects;

/**
 * Service responsible for generating enriched CSV output.
 *
 * Two sections:
 *   1. Sprint-level KPI summary block (header + one data row)
 *   2. Issue-level detail table (header + one row per issue)
 *
 * Opens cleanly in Excel / Google Sheets.
 * All null values are serialized as empty string — never "null".
 */
@Slf4j
@Service
public class CsvExportService {

    private static final String[] METRICS_HEADER = {
            "Committed SP", "Completed SP", "Remaining SP", "Spillover SP",
            "Velocity", "Predictability (%)", "Sprint Success",
            "Total Issues", "Completed Issues", "In Progress", "To Do",
            "Blocked Issues", "Bugs", "Stories", "Tasks",
            "Blocked Ratio (%)", "Bug Ratio (%)",
            "Delivered vs Committed (%)", "Sprint Focus Factor (%)",
            "Team Efficiency (%)", "Avg SP / Completed Issue",
            "Throughput", "Work In Progress", "Carry Over Issues",
            "Sprint Health Score (0-100)"
    };

    private static final String[] ISSUE_HEADER = {
            "Issue Key", "Summary", "Status", "Assignee", "Issue Type",
            "Committed SP", "Remaining SP", "Done SP"
    };

    public String exportToCsv(List<SprintIssue> issues, SprintMetrics metrics) {
        log.debug("Generating enriched CSV for {} issues", issues.size());

        StringWriter sw = new StringWriter();
        try (CSVWriter writer = new CSVWriter(sw)) {
            writer.writeNext(new String[]{"=== SPRINT KPI SUMMARY ==="});
            writer.writeNext(METRICS_HEADER);
            writer.writeNext(toMetricsRow(metrics));
            writer.writeNext(new String[]{""});
            writer.writeNext(new String[]{"=== ISSUE DETAIL ==="});
            writer.writeNext(ISSUE_HEADER);
            issues.stream().map(this::toIssueRow).forEach(writer::writeNext);
        } catch (Exception e) {
            log.error("Error generating CSV export", e);
            throw new RuntimeException("Failed to generate CSV export", e);
        }

        return sw.toString();
    }

    private String[] toMetricsRow(SprintMetrics m) {
        return new String[]{
                str(m.getCommittedStoryPoints()),
                str(m.getCompletedStoryPoints()),
                str(m.getRemainingStoryPoints()),
                str(m.getSpilloverStoryPoints()),
                str(m.getVelocity()),
                pct(m.getPredictabilityRate()),
                bool(m.getSprintSuccess()),
                str(m.getTotalIssues()),
                str(m.getCompletedIssues()),
                str(m.getInProgressIssues()),
                str(m.getTodoIssues()),
                str(m.getBlockedIssues()),
                str(m.getBugCount()),
                str(m.getStoryCount()),
                str(m.getTaskCount()),
                pct(m.getBlockedRatio()),
                pct(m.getBugRatio()),
                pct(m.getDeliveredVsCommittedRatio()),
                pct(m.getSprintFocusFactor()),
                pct(m.getTeamEfficiency()),
                dbl(m.getAverageSpPerCompletedIssue()),
                str(m.getThroughput()),
                str(m.getWorkInProgress()),
                str(m.getCarryOverIssues()),
                dbl(m.getSprintHealthScore())
        };
    }

    private String[] toIssueRow(SprintIssue issue) {
        return new String[]{
                issue.getIssueKey(),
                Objects.requireNonNullElse(issue.getSummary(), ""),
                Objects.requireNonNullElse(issue.getStatus(), ""),
                Objects.requireNonNullElse(issue.getAssignee(), "Unassigned"),
                Objects.requireNonNullElse(issue.getIssueType(), ""),
                str(issue.getTotalStoryPoints()),
                str(issue.getRemainingStoryPoints()),
                str(issue.getDoneStoryPoints())
        };
    }

    private String str(Integer v)  { return v != null ? v.toString() : ""; }
    private String str(int v)      { return Integer.toString(v); }
    private String dbl(Double v)   { return v != null ? String.format("%.2f", v) : ""; }
    private String pct(Double v)   { return v != null ? String.format("%.1f%%", v) : ""; }
    private String bool(Boolean v) { if (v == null) return ""; return v ? "Yes" : "No"; }
}
