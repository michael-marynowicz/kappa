package com.company.sprintreporter.domain.port;

import com.company.sprintreporter.domain.model.SprintIssue;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port: defines what the domain requires from a Jira data source.
 * The domain does NOT know about HTTP, REST or Jira specifics.
 * This interface will be implemented in the infrastructure layer.
 */
public interface JiraIssueRepository {

    /**
     * Fetch all issues for the currently configured sprint.
     */
    List<SprintIssue> fetchSprintIssues();
}
