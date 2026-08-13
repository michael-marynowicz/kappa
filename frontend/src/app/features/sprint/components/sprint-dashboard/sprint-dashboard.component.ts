import { Component, inject, OnInit, signal } from "@angular/core";
import { CurrentIterationService } from "../../services/current-iteration.service";
import { CommonModule } from "@angular/common";
import { ActivatedRoute, Router } from "@angular/router";
import { SprintStateService } from "../../services/sprint-state.service";
import { CapacityStateService } from "../../services/capacity-state.service";
import { SprintSummaryCardComponent } from "../sprint-summary-card/sprint-summary-card.component";
import { SprintIssueTableComponent } from "../sprint-issue-table/sprint-issue-table.component";
import { SprintAnalyticsComponent } from "../sprint-analytics/sprint-analytics.component";
import { CapacityGridComponent } from "../capacity-grid/capacity-grid.component";
import { PremiumOverlayComponent } from "../../../../shared/components/premium-overlay/premium-overlay.component";
import { TeamDashboardSwitcherComponent } from "../../../../shared/components/team-dashboard-switcher/team-dashboard-switcher.component";
import { TeamDashboardStateService } from "../../../../shared/services/team-dashboard-state.service";
import { JiraConfigApiService } from "../../../../core/services/jira-config-api.service";
import { JiraDashboard } from "../../../../core/models/jira-config.model";
import { AuthStateService } from "../../../../core/services/auth-state.service";
import { TranslatePipe } from "../../../../shared/pipes/translate.pipe";
import { I18nService } from "../../../../i18n/i18n.service";

@Component({
  selector: "app-sprint-dashboard",
  standalone: true,
  imports: [
    CommonModule,
    SprintSummaryCardComponent,
    SprintIssueTableComponent,
    SprintAnalyticsComponent,
    CapacityGridComponent,
    PremiumOverlayComponent,
    TeamDashboardSwitcherComponent,
    TranslatePipe,
  ],
  providers: [TeamDashboardStateService],
  templateUrl: "./sprint-dashboard.component.html",
  styleUrls: ["./sprint-dashboard.component.scss"],
})
export class SprintDashboardComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly jiraApi = inject(JiraConfigApiService);
  private readonly authState = inject(AuthStateService);
  readonly state = inject(SprintStateService);
  readonly capState = inject(CapacityStateService);
  readonly currentIteration = inject(CurrentIterationService);
  readonly i18n = inject(I18nService);
  readonly today = new Date();
  readonly dashboards = signal<JiraDashboard[]>([]);
  readonly dashboardsLoading = signal(false);
  readonly switchingDashboardId = signal<string | null>(null);
  readonly dashboardSwitchError = signal<string | null>(null);
  activeTab: "board" | "metrics" | "capacity" = "board";

  ngOnInit(): void {
    this.loadDashboards();
  }

  private loadDashboards(): void {
    this.dashboardsLoading.set(true);
    this.dashboardSwitchError.set(null);
    this.jiraApi.listDashboards().subscribe({
      next: (dashboards) => {
        const orderedDashboards = [...dashboards].sort(
          (a, b) => a.position - b.position,
        );
        this.dashboards.set(orderedDashboards);
        this.dashboardsLoading.set(false);
        this.activateFromQueryParam();
        if (orderedDashboards.some((d) => d.active)) {
          this.state.loadIssues();
          this.state.loadMetrics();
          this.state.loadIterations();
          this.currentIteration.fetch();
        }
      },
      error: (err) => {
        this.dashboardSwitchError.set(
          err.message ?? "Unable to load team dashboards.",
        );
        this.dashboardsLoading.set(false);
      },
    });
  }

  private activateFromQueryParam(): void {
    const dashboardId = this.route.snapshot.queryParamMap.get("dashboard");
    if (!dashboardId || !this.isAdmin) return;
    if (dashboardId === this.activeDashboardId) {
      // Already active — clear the query param silently
      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: {},
        replaceUrl: true,
      });
      return;
    }
    this.onSwitchDashboard(dashboardId);
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {},
      replaceUrl: true,
    });
  }

  get activeDashboardId(): string | null {
    return this.dashboards().find((dashboard) => dashboard.active)?.id ?? null;
  }

  get isAdmin(): boolean {
    return this.authState.user()?.role === "ADMIN";
  }

  onSwitchDashboard(dashboardId: string): void {
    if (!this.isAdmin) {
      return;
    }

    if (!dashboardId || dashboardId === this.activeDashboardId) {
      return;
    }

    this.switchingDashboardId.set(dashboardId);
    this.dashboardSwitchError.set(null);
    this.jiraApi.activateDashboard(dashboardId).subscribe({
      next: () => {
        this.switchingDashboardId.set(null);
        this.loadDashboards();
        this.state.loadIssues();
        this.state.loadMetrics();
        this.state.loadIterations();
        this.capState.loadGrid();
        this.currentIteration.fetch();
      },
      error: (err) => {
        this.dashboardSwitchError.set(
          err.message ?? "Unable to switch dashboard.",
        );
        this.switchingDashboardId.set(null);
      },
    });
  }
  get currentSprintName() {
    return this.currentIteration.name();
  }

  onUpdateRemainingStoryPoints(event: {
    issueKey: string;
    remainingStoryPoints: number;
  }): void {
    this.state.updateRemainingStoryPoints(
      event.issueKey,
      event.remainingStoryPoints,
    );
  }

  onExportCsv(): void {
    if (this.state.exportGated()) return;
    this.state.exportCsv();
  }

  onUpgrade(): void {
    this.router.navigate(["/settings/billing"]);
  }

  get hasDashboard(): boolean {
    return this.dashboards().some((d) => d.active);
  }

  get boardError(): string | null {
    return this.state.issuesError();
  }

  get metricsError(): string | null {
    return this.state.metricsError() ?? this.state.iterationsError();
  }

  get boardErrorStatus(): number | null {
    return this.state.issuesErrorStatus();
  }

  get metricsErrorStatus(): number | null {
    return (
      this.state.metricsErrorStatus() ?? this.state.iterationsErrorStatus()
    );
  }

  get capacityError(): string | null {
    return this.capState.error();
  }

  get capacityErrorStatus(): number | null {
    return this.capState.errorStatus();
  }

  get dashboardError(): string | null {
    // Dashboard load/switch errors are already surfaced inline next to the
    // team switcher (see [errorMessage] binding above) — do not repeat them
    // here, otherwise the same message would appear in two banners at once.
    if (this.activeTab === "metrics") {
      return this.metricsError;
    }

    if (this.activeTab === "capacity") {
      return this.capacityError;
    }

    return this.boardError;
  }

  get dashboardErrorStatus(): number | null {
    if (this.activeTab === "metrics") {
      return this.metricsErrorStatus;
    }

    if (this.activeTab === "capacity") {
      return this.capacityErrorStatus;
    }

    return this.boardErrorStatus;
  }

  get shouldShowGoToSettings(): boolean {
    return (
      this.dashboardErrorStatus === 502 || this.dashboardErrorStatus === 428
    );
  }

  onGoToJiraSettings(): void {
    this.router.navigate(["/settings/jira"]);
  }

  retryBoard(): void {
    this.state.loadIssues();
  }

  retryMetrics(): void {
    this.state.loadMetrics();
    this.state.loadIterations();
  }

  retryCapacity(): void {
    this.capState.loadGrid();
  }

  retryActiveTab(): void {
    if (this.activeTab === "metrics") {
      this.retryMetrics();
      return;
    }

    if (this.activeTab === "capacity") {
      this.retryCapacity();
      return;
    }

    this.retryBoard();
  }
}
