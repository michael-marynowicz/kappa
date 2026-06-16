package com.company.sprintreporter.service;

import com.company.sprintreporter.domain.model.SprintIssue;
import com.company.sprintreporter.domain.model.SprintMetrics;
import com.company.sprintreporter.domain.port.JiraIssueRepository;
import com.company.sprintreporter.domain.port.RemainingStoryPointsStore;
import com.company.sprintreporter.service.exception.IssueNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Core service: orchestrates domain logic.
 * All business decisions live here, NOT in controllers or infrastructure.
 *
 * Responsibilities:
 * - Fetch issues from Jira (via port, agnostic of implementation)
 * - Merge with user-provided remaining SP overrides
 * - Validate business rules before persisting
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SprintIssueService {

    private final JiraIssueRepository jiraIssueRepository;
    private final RemainingStoryPointsStore remainingStoryPointsStore;

    /**
     * Retrieve all sprint issues, merging Jira data with any stored remaining SP overrides.
     */
    public List<SprintIssue> getSprintIssues() {
        log.debug("Fetching sprint issues from Jira repository");
        List<SprintIssue> issues = jiraIssueRepository.fetchSprintIssues();

        // Merge in-memory remaining SP overrides onto the fetched issues
        var overrides = remainingStoryPointsStore.findAll();
        List<SprintIssue> merged = issues.stream()
                .map(issue -> {
                    Integer overrideValue = overrides.get(issue.getIssueKey());
                    if (overrideValue != null) {
                        return issue.withRemainingStoryPoints(overrideValue);
                    }
                    return issue;
                })
                .toList();

        log.debug("Returning {} issues ({} with SP overrides)", merged.size(), overrides.size());
        return merged;
    }

    /**
     * Compute all sprint-level Scrum Master metrics from current issue state.
     */
    public SprintMetrics getMetrics() {
        List<SprintIssue> issues = getSprintIssues();
        return SprintMetrics.compute(issues);
    }

    /**
     * Update the remaining story points for a given issue key.
     * Validates that the new value does not exceed total story points.
     */
    public SprintIssue updateRemainingStoryPoints(String issueKey, int remainingStoryPoints) {
        log.info("Updating remaining SP for issue {} to {}", issueKey, remainingStoryPoints);

        // Fetch fresh issue data to validate against current total
        List<SprintIssue> issues = jiraIssueRepository.fetchSprintIssues();
        SprintIssue issue = issues.stream()
                .filter(i -> i.getIssueKey().equals(issueKey))
                .findFirst()
                .orElseThrow(() -> new IssueNotFoundException(
                        "Issue with key '%s' not found in current sprint".formatted(issueKey)));

        // Re-create the issue with new remaining SP — enforces domain invariants
        SprintIssue updated = SprintIssue.create(
                issue.getIssueKey(),
                issue.getSummary(),
                issue.getStatus(),
                issue.getAssignee(),
                issue.getIssueType(),
                issue.getTotalStoryPoints(),
                remainingStoryPoints
        );

        remainingStoryPointsStore.save(issueKey, remainingStoryPoints);
        log.info("Remaining SP updated and persisted for issue {}", issueKey);
        return updated;
    }

    /**
     * Return all sprint issues with computed KPIs, ready for CSV export.
     * This is intentionally the same as getSprintIssues(); the export controller
     * handles the formatting concern.
     */
    public List<SprintIssue> getIssuesForExport() {
        return getSprintIssues();
    }
}
