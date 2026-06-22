import { Injectable, computed, inject, signal } from "@angular/core";
import { JiraConfigApiService } from "../../core/services/jira-config-api.service";
import { JiraDashboard } from "../../core/models/jira-config.model";

interface SwitchDashboardParams {
  dashboardId: string;
  isAdmin: boolean;
  onSuccess?: () => void;
  loadErrorMessage?: string;
  switchErrorMessage?: string;
}

@Injectable()
export class TeamDashboardStateService {
  private readonly jiraApi = inject(JiraConfigApiService);

  private readonly _dashboards = signal<JiraDashboard[]>([]);
  private readonly _loading = signal(false);
  private readonly _switchingDashboardId = signal<string | null>(null);
  private readonly _error = signal<string | null>(null);

  readonly dashboards = this._dashboards.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly switchingDashboardId = this._switchingDashboardId.asReadonly();
  readonly error = this._error.asReadonly();
  readonly activeDashboardId = computed(
    () => this._dashboards().find((dashboard) => dashboard.active)?.id ?? null,
  );

  loadDashboards(errorMessage = "Unable to load teams."): void {
    this._loading.set(true);
    this._error.set(null);
    this.jiraApi.listDashboards().subscribe({
      next: (dashboards) => {
        const orderedDashboards = [...dashboards].sort(
          (a, b) => a.position - b.position,
        );
        this._dashboards.set(orderedDashboards);
        this._loading.set(false);
      },
      error: (err) => {
        this._error.set(err.message ?? errorMessage);
        this._loading.set(false);
      },
    });
  }

  switchDashboard(params: SwitchDashboardParams): void {
    const {
      dashboardId,
      isAdmin,
      onSuccess,
      loadErrorMessage = "Unable to load teams.",
      switchErrorMessage = "Unable to switch team.",
    } = params;

    if (!isAdmin) {
      return;
    }

    if (!dashboardId || dashboardId === this.activeDashboardId()) {
      return;
    }

    this._switchingDashboardId.set(dashboardId);
    this._error.set(null);
    this.jiraApi.activateDashboard(dashboardId).subscribe({
      next: () => {
        this._switchingDashboardId.set(null);
        this.loadDashboards(loadErrorMessage);
        onSuccess?.();
      },
      error: (err) => {
        this._error.set(err.message ?? switchErrorMessage);
        this._switchingDashboardId.set(null);
      },
    });
  }
}
