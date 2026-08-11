import { Component, inject, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { CapacityGridComponent } from "../sprint/components/capacity-grid/capacity-grid.component";
import { CapacityStateService } from "../sprint/services/capacity-state.service";
import { AuthStateService } from "../../core/services/auth-state.service";
import { TeamDashboardSwitcherComponent } from "../../shared/components/team-dashboard-switcher/team-dashboard-switcher.component";
import { TeamDashboardStateService } from "../../shared/services/team-dashboard-state.service";
import { TranslatePipe } from "../../shared/pipes/translate.pipe";

@Component({
  selector: "app-capacity-page",
  standalone: true,
  imports: [
    CommonModule,
    CapacityGridComponent,
    TeamDashboardSwitcherComponent,
    TranslatePipe,
  ],
  providers: [TeamDashboardStateService],
  templateUrl: "./capacity-page.component.html",
  styles: [
    `
      .capacity-page {
        max-width: 1200px;
      }
      .capacity__header {
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
      .capacity__header > .btn-export {
        justify-self: end;
      }
      .capacity__header > div:first-child {
        justify-self: start;
      }
      @media (max-width: 1024px) {
        .capacity__header {
          grid-template-columns: 1fr;
        }
        .capacity__header > div:first-child,
        .header-actions,
        .capacity__header > .btn-export {
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
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 8px 16px;
        background: rgba(255, 255, 255, 0.04);
        border: 1px solid rgba(255, 255, 255, 0.1);
        border-radius: 8px;
        color: #e4e7ef;
        font-size: 13px;
        font-weight: 500;
        cursor: pointer;
        transition: background 150ms ease;
      }
      .btn-export:hover {
        background: rgba(255, 255, 255, 0.08);
      }
    `,
  ],
})
export class CapacityPageComponent implements OnInit {
  private readonly authState = inject(AuthStateService);
  private readonly teamDash = inject(TeamDashboardStateService);

  readonly capState = inject(CapacityStateService);
  readonly dashboards = this.teamDash.dashboards;
  readonly dashboardsLoading = this.teamDash.loading;
  readonly switchingDashboardId = this.teamDash.switchingDashboardId;
  readonly dashboardError = this.teamDash.error;

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
      onSuccess: () => this.capState.loadGrid(),
    });
  }

  onExportCsv(): void {
    this.capState.exportXlsx();
  }
}
