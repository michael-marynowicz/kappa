package com.company.sprintreporter.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Aggregated sprint-level metrics computed from the full list of sprint issues.
 *
 * All metrics follow standard Scrum / Agile definitions.
 * Computed once from the issues list; immutable value object.
 *
 * Null-safe throughout: missing story points never cause NPE,
 * they are treated as 0 in aggregate calculations.
 */
@Getter
@Builder
public class SprintMetrics {

    // ── Story Points ──────────────────────────────────────────────────────────
    private final int committedStoryPoints;
    private final int completedStoryPoints;
    private final int remainingStoryPoints;
    private final int spilloverStoryPoints;

    // ── Velocity & Predictability ─────────────────────────────────────────────
    /** Velocity = completed SP this sprint */
    private final int velocity;

    /**
     * Predictability = (completed / committed) × 100.
     * Null when committed = 0.
     */
    private final Double predictabilityRate;

    /**
     * Sprint Success: committed == 0 → null; else completed >= committed.
     */
    private final Boolean sprintSuccess;

    // ── Issue Counts ──────────────────────────────────────────────────────────
    private final int totalIssues;
    private final int completedIssues;
    private final int inProgressIssues;
    private final int todoIssues;
    private final int blockedIssues;
    private final int bugCount;
    private final int storyCount;
    private final int taskCount;

    // ── Ratios ────────────────────────────────────────────────────────────────
    /** Blocked ratio = blocked / total × 100 */
    private final Double blockedRatio;

    /** Bug ratio = bugs / total × 100 */
    private final Double bugRatio;

    /**
     * Delivered vs committed ratio = completedSP / committedSP × 100.
     * Alias of predictabilityRate, surfaced separately for CSV clarity.
     */
    private final Double deliveredVsCommittedRatio;

    /**
     * Sprint Focus Factor = completed SP / available capacity × 100.
     * Approximated as completedSP / committedSP when capacity is not tracked.
     */
    private final Double sprintFocusFactor;

    /**
     * Team Efficiency = completedIssues / totalIssues × 100
     */
    private final Double teamEfficiency;

    /**
     * Throughput = number of completed issues (count, not SP)
     */
    private final int throughput;

    /**
     * Work In Progress = issues currently In Progress or In Review
     */
    private final int workInProgress;

    /**
     * Carry Over = issues not completed at sprint end (remaining > 0 or status != Done)
     */
    private final int carryOverIssues;

    /**
     * Average story points per completed issue
     */
    private final Double averageSpPerCompletedIssue;

    /**
     * Sprint Health Score (0–100): composite of predictability, bug ratio, blocked ratio.
     * Formula: 0.5 × predictability + 0.3 × (100 - bugRatio) + 0.2 × (100 - blockedRatio)
     */
    private final Double sprintHealthScore;

    // ── Factory ───────────────────────────────────────────────────────────────

    /**
     * Compute all sprint metrics from the live issues list.
     * Single entry point — all metrics stay consistent with each other.
     */
    public static SprintMetrics compute(List<SprintIssue> issues) {
        if (issues == null || issues.isEmpty()) {
            return empty();
        }

        // Raw counts
        int totalIssues     = issues.size();
        int committedSP     = sum(issues, SprintIssue::getTotalStoryPoints);
        int completedSP     = issues.stream()
                .filter(SprintMetrics::isDone)
                .mapToInt(i -> Objects.requireNonNullElse(i.getTotalStoryPoints(), 0))
                .sum();
        int remainingSP     = committedSP - completedSP;
        int spilloverSP     = issues.stream()
                .filter(i -> !isDone(i))
                .mapToInt(i -> Objects.requireNonNullElse(i.getRemainingStoryPoints(),
                        Objects.requireNonNullElse(i.getTotalStoryPoints(), 0)))
                .sum();

        int completedIssues = (int) issues.stream().filter(SprintMetrics::isDone).count();
        int inProgress      = (int) issues.stream().filter(SprintMetrics::isInProgress).count();
        int todo            = (int) issues.stream().filter(SprintMetrics::isTodo).count();
        int blocked         = (int) issues.stream().filter(SprintMetrics::isBlocked).count();
        int wip             = inProgress + (int) issues.stream().filter(SprintMetrics::isInReview).count();

        int bugs            = (int) issues.stream()
                .filter(i -> "Bug".equalsIgnoreCase(i.getIssueType())).count();
        int stories         = (int) issues.stream()
                .filter(i -> "Story".equalsIgnoreCase(i.getIssueType())).count();
        int tasks           = (int) issues.stream()
                .filter(i -> "Task".equalsIgnoreCase(i.getIssueType())).count();

        int carryOver       = (int) issues.stream().filter(i -> !isDone(i)).count();

        // Ratios (null-safe)
        Double predictability   = committedSP > 0 ? round((double) completedSP / committedSP * 100) : null;
        Double bugRatio         = totalIssues > 0 ? round((double) bugs / totalIssues * 100) : null;
        Double blockedRatio     = totalIssues > 0 ? round((double) blocked / totalIssues * 100) : null;
        Double efficiency       = totalIssues > 0 ? round((double) completedIssues / totalIssues * 100) : null;
        Double focusFactor      = committedSP > 0 ? round((double) completedSP / committedSP * 100) : null;
        Double avgSpPerDone     = completedIssues > 0 ? round((double) completedSP / completedIssues) : null;
        Boolean success         = committedSP > 0 ? completedSP >= committedSP : null;

        // Sprint Health Score
        Double healthScore = null;
        if (predictability != null && bugRatio != null && blockedRatio != null) {
            healthScore = round(
                    0.50 * predictability
                    + 0.30 * (100 - bugRatio)
                    + 0.20 * (100 - blockedRatio)
            );
        }

        return SprintMetrics.builder()
                .committedStoryPoints(committedSP)
                .completedStoryPoints(completedSP)
                .remainingStoryPoints(remainingSP)
                .spilloverStoryPoints(spilloverSP)
                .velocity(completedSP)
                .predictabilityRate(predictability)
                .sprintSuccess(success)
                .totalIssues(totalIssues)
                .completedIssues(completedIssues)
                .inProgressIssues(inProgress)
                .todoIssues(todo)
                .blockedIssues(blocked)
                .bugCount(bugs)
                .storyCount(stories)
                .taskCount(tasks)
                .blockedRatio(blockedRatio)
                .bugRatio(bugRatio)
                .deliveredVsCommittedRatio(predictability)
                .sprintFocusFactor(focusFactor)
                .teamEfficiency(efficiency)
                .throughput(completedIssues)
                .workInProgress(wip)
                .carryOverIssues(carryOver)
                .averageSpPerCompletedIssue(avgSpPerDone)
                .sprintHealthScore(healthScore)
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static int sum(List<SprintIssue> issues,
                           java.util.function.Function<SprintIssue, Integer> fn) {
        return issues.stream()
                .mapToInt(i -> Objects.requireNonNullElse(fn.apply(i), 0))
                .sum();
    }

    private static boolean isDone(SprintIssue i) {
        String s = i.getStatus();
        return "Done".equalsIgnoreCase(s) || "Completed".equalsIgnoreCase(s);
    }

    private static boolean isInProgress(SprintIssue i) {
        String s = i.getStatus();
        return "In Progress".equalsIgnoreCase(s) || "Implementing".equalsIgnoreCase(s);
    }

    private static boolean isInReview(SprintIssue i) {
        return "In Review".equalsIgnoreCase(i.getStatus());
    }

    private static boolean isTodo(SprintIssue i) {
        String s = i.getStatus();
        return "To Do".equalsIgnoreCase(s) || "Open".equalsIgnoreCase(s)
                || "Backlog".equalsIgnoreCase(s);
    }

    private static boolean isBlocked(SprintIssue i) {
        return "Blocked".equalsIgnoreCase(i.getStatus());
    }

    private static double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static SprintMetrics empty() {
        return SprintMetrics.builder()
                .committedStoryPoints(0).completedStoryPoints(0)
                .remainingStoryPoints(0).spilloverStoryPoints(0)
                .velocity(0).totalIssues(0).completedIssues(0)
                .inProgressIssues(0).todoIssues(0).blockedIssues(0)
                .bugCount(0).storyCount(0).taskCount(0)
                .throughput(0).workInProgress(0).carryOverIssues(0)
                .build();
    }
}
