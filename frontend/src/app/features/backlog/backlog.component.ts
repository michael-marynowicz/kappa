import { Component, inject, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterLink } from "@angular/router";
import { SprintStateService } from "../sprint/services/sprint-state.service";
import { SprintIssueTableComponent } from "../sprint/components/sprint-issue-table/sprint-issue-table.component";
import { SprintSummaryCardComponent } from "../sprint/components/sprint-summary-card/sprint-summary-card.component";
import { AuthStateService } from "../../core/services/auth-state.service";
import { TeamDashboardSwitcherComponent } from "../../shared/components/team-dashboard-switcher/team-dashboard-switcher.component";
import { TeamDashboardStateService } from "../../shared/services/team-dashboard-state.service";
import { TranslatePipe } from "../../shared/pipes/translate.pipe";

@Component({
  selector: "app-backlog",
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    SprintIssueTableComponent,
    SprintSummaryCardComponent,
    TeamDashboardSwitcherComponent,
    TranslatePipe,
  ],
  providers: [TeamDashboardStateService],
  templateUrl: "./backlog.component.html",
  styles: [
    `
      .backlog {
        max-width: 1200px;
      }
      .backlog__header {
        display: grid;
        grid-template-columns: minmax(240px, 1fr) auto minmax(160px, 1fr);
        align-items: center;
        gap: 12px;
        margin-bottom: 24px;
      }
      .header-actions {
        display: flex;
        align-items: center;
        gap: 10px;
        justify-self: center;
      }
      .header-text {
        justify-self: start;
      }
      .backlog__header > .btn-export {
        justify-self: end;
      }
      @media (max-width: 1024px) {
        .backlog__header {
          grid-template-columns: 1fr;
        }
        .header-text,
        .header-actions,
        .backlog__header > .btn-export {
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
      .btn-export {
        padding: 8px 16px;
        background: rgba(255, 255, 255, 0.04);
        border: 1px solid rgba(255, 255, 255, 0.1);
        border-radius: 8px;
        color: #e4e7ef;
        font-size: 13px;
        cursor: pointer;
      }
      .btn-export:hover {
        background: rgba(255, 255, 255, 0.08);
      }
      /* ── Skeleton ─────────────────────────────────── */
      @keyframes bsk-shimmer {
        100% {
          transform: translateX(100%);
        }
      }
      .backlog-skeleton {
        display: flex;
        flex-direction: column;
        gap: 0;
        border: 1px solid rgba(255, 255, 255, 0.05);
        border-radius: 12px;
        overflow: hidden;
      }
      .backlog-skeleton__row {
        display: grid;
        grid-template-columns: 90px 1fr 100px 64px;
        gap: 14px;
        align-items: center;
        padding: 14px 16px;
        border-bottom: 1px solid rgba(255, 255, 255, 0.04);
        &:last-child {
          border-bottom: none;
        }
      }
      .backlog-skeleton__cell {
        position: relative;
        overflow: hidden;
        height: 13px;
        border-radius: 6px;
        background: rgba(255, 255, 255, 0.07);
        &::after {
          content: "";
          position: absolute;
          inset: 0;
          transform: translateX(-100%);
          background: linear-gradient(
            90deg,
            transparent,
            rgba(255, 255, 255, 0.14),
            transparent
          );
          animation: bsk-shimmer 1.25s ease-in-out infinite;
        }
      }
      .bsk--key {
        width: 70%;
      }
      .bsk--title {
      }
      .bsk--badge {
        width: 60%;
        border-radius: 20px;
      }
      .bsk--num {
        width: 40%;
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
      }
      .error-banner button {
        background: none;
        border: none;
        color: #f87171;
        cursor: pointer;
      }
    `,
  ],
})
export class BacklogComponent implements OnInit {
  private readonly authState = inject(AuthStateService);
  private readonly teamDash = inject(TeamDashboardStateService);

  readonly state = inject(SprintStateService);
  readonly dashboards = this.teamDash.dashboards;
  readonly dashboardsLoading = this.teamDash.loading;
  readonly switchingDashboardId = this.teamDash.switchingDashboardId;
  readonly dashboardError = this.teamDash.error;

  ngOnInit(): void {
    this.teamDash.loadDashboards("Unable to load teams.");
    this.state.loadIssues();
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
      onSuccess: () => this.state.loadIssues(),
    });
  }

  onUpdateSp(event: { issueKey: string; remainingStoryPoints: number }): void {
    this.state.updateRemainingStoryPoints(
      event.issueKey,
      event.remainingStoryPoints,
    );
  }

  onExportCsv(): void {
    this.state.exportCsv();
  }
}
