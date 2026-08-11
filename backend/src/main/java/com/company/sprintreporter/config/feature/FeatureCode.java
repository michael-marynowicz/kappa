package com.company.sprintreporter.config.feature;

/**
 * Centralized feature code constants matching the DB `features.code` values.
 * Used in {@link RequiresFeature} annotations for compile-time safety.
 *
 * Plan mapping:
 * ┌─────────────────────────┬──────┬─────┬────────────┐
 * │ Feature                 │ FREE │ PRO │ ENTERPRISE │
 * ├─────────────────────────┼──────┼─────┼────────────┤
 * │ SPRINT_TRACKING         │  ✓   │  ✓  │     ✓      │
 * │ METRICS_DASHBOARD       │  ✓   │  ✓  │     ✓      │
 * │ CSV_EXPORT              │      │  ✓  │     ✓      │
 * │ JIRA_INTEGRATION        │      │  ✓  │     ✓      │
 * │ ITERATION_COMPARISON    │      │  ✓  │     ✓      │
 * │ CAPACITY_PLANNING       │      │  ✓  │     ✓      │
 * │ METRICS_ADVANCED        │      │  ✓  │     ✓      │
 * │ MULTI_BOARD             │      │     │     ✓      │
 * │ API_ACCESS              │      │  ✓  │     ✓      │
 * │ CUSTOM_REPORTS          │      │     │     ✓      │
 * │ PRIORITY_SUPPORT        │      │     │     ✓      │
 * └─────────────────────────┴──────┴─────┴────────────┘
 *
 * Business rule: METRICS_ADVANCED (average velocity computation)
 * depends on CAPACITY_PLANNING data being populated.
 */
public final class FeatureCode {

    private FeatureCode() {}

    /** Basic sprint backlog tracking (view/edit issues, story points) */
    public static final String SPRINT_TRACKING = "sprint_tracking";

    /** Basic metrics dashboard (velocity, committed/delivered, topic breakdown) */
    public static final String METRICS_DASHBOARD = "metrics_dashboard";

    /** Export sprint reports as CSV */
    public static final String CSV_EXPORT = "csv_export";

    /** Connect to Jira and sync data */
    public static final String JIRA_INTEGRATION = "jira_integration";

    /** Compare metrics across past sprints (iteration history) */
    public static final String ITERATION_COMPARISON = "iteration_comparison";

    /** Team capacity and availability planning */
    public static final String CAPACITY_PLANNING = "capacity_planning";

    /** Advanced metrics: average velocity, trend analysis (requires capacity data) */
    public static final String METRICS_ADVANCED = "metrics_advanced";

    /** Track multiple Jira boards */
    public static final String MULTI_BOARD = "multi_board";

    /** Programmatic REST API access */
    public static final String API_ACCESS = "api_access";

    /** Custom report templates */
    public static final String CUSTOM_REPORTS = "custom_reports";

    /** 24h response priority support */
    public static final String PRIORITY_SUPPORT = "priority_support";
}
