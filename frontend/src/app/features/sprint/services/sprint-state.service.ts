import { Injectable, inject, signal, computed } from "@angular/core";
import { SprintApiService } from "./sprint-api.service";
import {
  SprintIssue,
  SprintMetrics,
  SprintSummary,
  IterationSnapshot,
  EpicGroup,
} from "../models/sprint-issue.model";

/** Check if an error is a feature-gated 403 from the interceptor. */
function isFeatureGatedError(err: unknown): boolean {
  return (
    typeof err === "object" &&
    err !== null &&
    "featureGated" in err &&
    (err as any).featureGated === true
  );
}

@Injectable({ providedIn: "root" })
export class SprintStateService {
  private readonly apiService = inject(SprintApiService);

  private readonly _issues = signal<SprintIssue[]>([]);
  private readonly _metrics = signal<SprintMetrics | null>(null);
  private readonly _iterations = signal<IterationSnapshot[]>([]);
  private readonly _epicGroups = signal<EpicGroup[]>([]);
  private readonly _loading = signal<boolean>(false);
  private readonly _metricsLoading = signal<boolean>(false);
  private readonly _error = signal<string | null>(null);
  private readonly _savingIssueKey = signal<string | null>(null);
  private readonly _metricsGated = signal(false);
  private readonly _iterationsGated = signal(false);
  private readonly _exportGated = signal(false);

  readonly issues = this._issues.asReadonly();
  readonly metrics = this._metrics.asReadonly();
  readonly iterations = this._iterations.asReadonly();
  readonly epicGroups = this._epicGroups.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly metricsLoading = this._metricsLoading.asReadonly();
  readonly error = this._error.asReadonly();
  readonly savingIssueKey = this._savingIssueKey.asReadonly();
  readonly metricsGated = this._metricsGated.asReadonly();
  readonly iterationsGated = this._iterationsGated.asReadonly();
  readonly exportGated = this._exportGated.asReadonly();

  readonly summary = computed<SprintSummary>(() => {
    const issues = this._issues();
    const totalSP = issues.reduce((s, i) => s + (i.totalStoryPoints ?? 0), 0);
    const remainSP = issues.reduce(
      (s, i) => s + (i.remainingStoryPoints ?? 0),
      0,
    );
    const doneSP = issues.reduce((s, i) => s + (i.doneStoryPoints ?? 0), 0);
    return {
      totalIssues: issues.length,
      totalStoryPoints: totalSP,
      remainingStoryPoints: remainSP,
      doneStoryPoints: doneSP,
      completionPercentage:
        totalSP > 0 ? Math.round((doneSP / totalSP) * 100) : 0,
    };
  });

  loadIssues(): void {
    this._loading.set(true);
    this._error.set(null);
    this.apiService.getSprintIssues().subscribe({
      next: (issues) => {
        this._issues.set(issues);
        this._loading.set(false);
      },
      error: (err) => {
        this._error.set(err.message ?? "Failed to load sprint issues");
        this._loading.set(false);
      },
    });
  }

  loadMetrics(): void {
    this._metricsLoading.set(true);
    this.apiService.getMetrics().subscribe({
      next: (m) => {
        this._metrics.set(m);
        this._metricsGated.set(false);
        this._metricsLoading.set(false);
      },
      error: (err) => {
        if (isFeatureGatedError(err)) {
          this._metricsGated.set(true);
        } else {
          this._error.set(err.message ?? "Failed to load metrics");
        }
        this._metricsLoading.set(false);
      },
    });
  }

  loadIterations(): void {
    this.apiService.getIterationHistory().subscribe({
      next: (data) => {
        this._iterations.set(data);
        this._iterationsGated.set(false);
      },
      error: (err) => {
        if (isFeatureGatedError(err)) {
          this._iterationsGated.set(true);
        } else {
          this._error.set(err.message ?? "Failed to load iteration history");
        }
      },
    });
  }

  loadGroupedIssues(): void {
    this.apiService.getGroupedIssues().subscribe({
      next: (groups) => this._epicGroups.set(groups),
      error: (err) => {
        this._error.set(err.message ?? "Failed to load grouped issues");
      },
    });
  }

  updateRemainingStoryPoints(
    issueKey: string,
    remainingStoryPoints: number,
  ): void {
    this._savingIssueKey.set(issueKey);
    this.apiService
      .updateRemainingStoryPoints({ issueKey, remainingStoryPoints })
      .subscribe({
        next: (updated) => {
          this._issues.update((issues) =>
            issues.map((i) => (i.issueKey === updated.issueKey ? updated : i)),
          );
          this._savingIssueKey.set(null);
          // Refresh metrics after SP update
          this.loadMetrics();
        },
        error: (err) => {
          this._error.set(err.message ?? "Failed to update story points");
          this._savingIssueKey.set(null);
        },
      });
  }

  exportCsv(): void {
    this.apiService.exportCsv().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `sprint-report-${new Date().toISOString().split("T")[0]}.csv`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        if (isFeatureGatedError(err)) {
          this._exportGated.set(true);
        } else {
          this._error.set(err.message ?? "Failed to export CSV");
        }
      },
    });
  }

  clearError(): void {
    this._error.set(null);
  }
}
