import {
  Component,
  computed,
  effect,
  inject,
  OnInit,
  signal,
  untracked,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { SprintStateService } from "../sprint/services/sprint-state.service";
import {
  ChartFocusView,
  SprintAnalyticsComponent,
} from "../sprint/components/sprint-analytics/sprint-analytics.component";
import { PremiumOverlayComponent } from "../../shared/components/premium-overlay/premium-overlay.component";
import { EmptyStateComponent } from "../../shared/components/empty-state/empty-state.component";
import { AuthStateService } from "../../core/services/auth-state.service";
import { PermissionService } from "../../core/services/permission.service";
import { Permission } from "../../core/models/permission.model";
import { TeamDashboardSwitcherComponent } from "../../shared/components/team-dashboard-switcher/team-dashboard-switcher.component";
import { TeamDashboardStateService } from "../../shared/services/team-dashboard-state.service";
import { I18nService } from "../../i18n/i18n.service";
import { TranslatePipe } from "../../shared/pipes/translate.pipe";

@Component({
  selector: "app-metrics",
  standalone: true,
  imports: [
    CommonModule,
    SprintAnalyticsComponent,
    PremiumOverlayComponent,
    EmptyStateComponent,
    TeamDashboardSwitcherComponent,
    TranslatePipe,
  ],
  providers: [TeamDashboardStateService],
  templateUrl: "./metrics.component.html",
  styles: [
    `
      .metrics-page {
        width: 100%;
      }
      .metrics__header {
        display: grid;
        grid-template-columns: minmax(240px, 1fr) auto minmax(160px, 1fr);
        align-items: center;
        gap: 12px;
        margin-bottom: 16px;
      }
      .header-actions {
        display: flex;
        justify-self: center;
      }
      .metrics__spacer {
        min-height: 1px;
      }
      @media (max-width: 1024px) {
        .metrics__header {
          grid-template-columns: 1fr;
        }
        .header-actions,
        .metrics__spacer {
          justify-self: start;
        }
      }
      .page-title {
        font-size: 22px;
        font-weight: 700;
        color: #e4e7ef;
        margin: 0 0 4px;
      }
      .page-subtitle {
        font-size: 13px;
        color: #8b92a8;
        margin: 0;
      }

      /* ── Segmented control ─────────────────────────── */
      .chart-switcher {
        display: inline-flex;
        gap: 4px;
        background: rgba(30, 34, 48, 0.6);
        border: 1px solid rgba(255, 255, 255, 0.06);
        border-radius: 10px;
        padding: 4px;
        margin-bottom: 20px;
      }
      .chart-switcher__btn {
        font-family: inherit;
        font-size: 13px;
        font-weight: 500;
        color: #8b92a8;
        background: transparent;
        border: none;
        border-radius: 7px;
        padding: 7px 16px;
        cursor: pointer;
        transition:
          background 0.2s,
          color 0.2s,
          box-shadow 0.2s;

        &:hover:not(.chart-switcher__btn--active) {
          color: #c5cbe0;
          background: rgba(255, 255, 255, 0.04);
        }
      }
      .chart-switcher__btn--active {
        color: #fff;
        background: rgba(96, 165, 250, 0.15);
        box-shadow:
          0 1px 4px rgba(96, 165, 250, 0.12),
          inset 0 0 0 1px rgba(96, 165, 250, 0.25);
      }

      .error-banner {
        display: flex;
        align-items: center;
        justify-content: space-between;
        background: rgba(248, 113, 113, 0.08);
        border: 1px solid rgba(248, 113, 113, 0.2);
        border-radius: 8px;
        padding: 10px 14px;
        font-size: 12px;
        color: #f87171;
        margin-bottom: 16px;
        button {
          background: none;
          border: none;
          color: #f87171;
          cursor: pointer;
        }
      }

      .metrics-skeleton {
        display: flex;
        flex-direction: column;
        gap: 16px;
      }
      .metrics-skeleton__row {
        display: grid;
        gap: 12px;
      }
      .metrics-skeleton__row--summary {
        grid-template-columns: repeat(3, minmax(0, 1fr));
      }
      .metrics-skeleton__row--chart {
        grid-template-columns: minmax(0, 2fr) minmax(220px, 1fr);
      }
      .metrics-skeleton__row--table {
        grid-template-columns: 1fr;
      }
      .metrics-skeleton__card,
      .metrics-skeleton__chart,
      .metrics-skeleton__list,
      .metrics-skeleton__line {
        position: relative;
        overflow: hidden;
        border-radius: 12px;
        background: rgba(255, 255, 255, 0.06);
      }
      .metrics-skeleton__card {
        height: 92px;
      }
      .metrics-skeleton__chart {
        height: 260px;
      }
      .metrics-skeleton__list {
        padding: 14px;
        display: grid;
        gap: 10px;
        align-content: start;
      }
      .metrics-skeleton__line {
        height: 12px;
        width: 70%;
      }
      .metrics-skeleton__line--full {
        width: 100%;
      }
      .metrics-skeleton__card::after,
      .metrics-skeleton__chart::after,
      .metrics-skeleton__list::after,
      .metrics-skeleton__line::after {
        content: "";
        position: absolute;
        inset: 0;
        transform: translateX(-100%);
        background: linear-gradient(
          90deg,
          transparent,
          rgba(255, 255, 255, 0.16),
          transparent
        );
        animation: skeleton-shimmer 1.25s ease-in-out infinite;
      }

      @media (max-width: 960px) {
        .metrics-skeleton__row--summary,
        .metrics-skeleton__row--chart {
          grid-template-columns: 1fr;
        }
      }

      @keyframes skeleton-shimmer {
        100% {
          transform: translateX(100%);
        }
      }
    `,
  ],
})
export class MetricsComponent implements OnInit {
  private readonly authState = inject(AuthStateService);
  private readonly teamDash = inject(TeamDashboardStateService);
  readonly permService = inject(PermissionService);

  readonly state = inject(SprintStateService);
  readonly dashboards = this.teamDash.dashboards;
  readonly dashboardsLoading = this.teamDash.loading;
  readonly switchingDashboardId = this.teamDash.switchingDashboardId;
  readonly dashboardError = this.teamDash.error;

  private metricsLoadedOnce = false;

  constructor() {
    effect(() => {
      const dashboards = this.teamDash.dashboards();
      if (
        !this.teamDash.loading() &&
        !this.metricsLoadedOnce &&
        dashboards.some((d) => d.active)
      ) {
        this.metricsLoadedOnce = true;
        untracked(() => {
          this.state.loadMetrics();
          this.state.loadIterations();
          this.state.loadGroupedIssues();
        });
      }
    });
  }

  readonly noDashboardConfigured = computed(() => {
    return (
      !this.teamDash.loading() &&
      !this.teamDash.error() &&
      !this.teamDash.dashboards().some((d) => d.active)
    );
  });

  readonly hasMetricsAccess = this.permService.hasPermissionSignal(
    Permission.METRICS_BASIC,
  );

  readonly showAnalyticsSkeleton = computed(() => {
    if (this.noDashboardConfigured()) return false;
    const loadingByRequest = this.state.metricsLoading();
    const loadingBySwitch = this.switchingDashboardId() !== null;
    const hasError =
      !!this.state.metricsError() ||
      !!this.state.iterationsError() ||
      !!this.state.error();
    const initialEmptyState =
      this.state.metrics() === null && !this.state.metricsGated() && !hasError;

    return (
      !hasError && (loadingByRequest || loadingBySwitch || initialEmptyState)
    );
  });

  readonly activeError = computed(
    () =>
      this.state.metricsError() ??
      this.state.iterationsError() ??
      this.state.error(),
  );

  clearActiveError(): void {
    this.state.clearError();
  }

  readonly focusView = signal<ChartFocusView>("all");
  private readonly i18n = inject(I18nService);

  get viewOptions(): { value: ChartFocusView; label: string }[] {
    return [
      { value: "all", label: this.i18n.t("metrics.view.all") },
      { value: "velocity", label: this.i18n.t("metrics.view.velocity") },
      { value: "capacity", label: this.i18n.t("metrics.view.capacity") },
      { value: "topics", label: this.i18n.t("metrics.view.topics") },
    ];
  }

  ngOnInit(): void {
    this.teamDash.loadDashboards("Unable to load teams.");
  }

  get activeDashboardId(): string | null {
    return this.teamDash.activeDashboardId();
  }

  get isAdmin(): boolean {
    return this.authState.user()?.role === "ADMIN";
  }

  onSwitchTeam(dashboardId: string): void {
    if (!this.isAdmin) {
      return;
    }

    if (!dashboardId || dashboardId === this.activeDashboardId) {
      return;
    }

    this.teamDash.switchDashboard({
      dashboardId,
      isAdmin: this.isAdmin,
      loadErrorMessage: "Unable to load teams.",
      switchErrorMessage: "Unable to switch team.",
      onSuccess: () => {
        this.state.loadMetrics();
        this.state.loadIterations();
        this.state.loadGroupedIssues();
      },
    });
  }
}
