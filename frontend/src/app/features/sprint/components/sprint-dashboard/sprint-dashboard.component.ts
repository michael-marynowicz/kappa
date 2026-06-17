import { Component, inject, OnInit } from "@angular/core";
import { CurrentIterationService } from "../../services/current-iteration.service";
import { CommonModule } from "@angular/common";
import { Router } from "@angular/router";
import { SprintStateService } from "../../services/sprint-state.service";
import { CapacityStateService } from "../../services/capacity-state.service";
import { SprintSummaryCardComponent } from "../sprint-summary-card/sprint-summary-card.component";
import { SprintIssueTableComponent } from "../sprint-issue-table/sprint-issue-table.component";
import { SprintAnalyticsComponent } from "../sprint-analytics/sprint-analytics.component";
import { CapacityGridComponent } from "../capacity-grid/capacity-grid.component";
import { PremiumOverlayComponent } from "../../../../shared/components/premium-overlay/premium-overlay.component";

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
  ],
  templateUrl: "./sprint-dashboard.component.html",
  styleUrls: ["./sprint-dashboard.component.scss"],
})
export class SprintDashboardComponent implements OnInit {
  private readonly router = inject(Router);
  readonly state = inject(SprintStateService);
  readonly capState = inject(CapacityStateService);
  readonly currentIteration = inject(CurrentIterationService);
  readonly today = new Date();
  activeTab: "board" | "metrics" | "capacity" = "board";

  ngOnInit(): void {
    this.state.loadIssues();
    this.state.loadMetrics();
    this.state.loadIterations();
    this.currentIteration.fetch();
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

  get boardError(): string | null {
    return this.state.issuesError();
  }

  get metricsError(): string | null {
    return this.state.metricsError() ?? this.state.iterationsError();
  }

  get capacityError(): string | null {
    return this.capState.error();
  }

  get dashboardError(): string | null {
    if (this.activeTab === "metrics") {
      return this.metricsError;
    }

    if (this.activeTab === "capacity") {
      return this.capacityError;
    }

    return this.boardError;
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
