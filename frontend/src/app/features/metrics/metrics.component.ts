import { Component, inject, OnInit, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import { SprintStateService } from "../sprint/services/sprint-state.service";
import { CapacityStateService } from "../sprint/services/capacity-state.service";
import { SprintAnalyticsComponent } from "../sprint/components/sprint-analytics/sprint-analytics.component";
import { ChartFocusView } from "../sprint/components/sprint-analytics/sprint-analytics.component";
import { PremiumOverlayComponent } from "../../shared/components/premium-overlay/premium-overlay.component";

@Component({
  selector: "app-metrics",
  standalone: true,
  imports: [CommonModule, SprintAnalyticsComponent, PremiumOverlayComponent],
  templateUrl: "./metrics.component.html",
  styles: [
    `
      .metrics-page {
        width: 100%;
      }
      .metrics__header {
        margin-bottom: 16px;
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
    `,
  ],
})
export class MetricsComponent implements OnInit {
  readonly state = inject(SprintStateService);
  readonly capState = inject(CapacityStateService);

  readonly focusView = signal<ChartFocusView>("all");

  readonly viewOptions: { value: ChartFocusView; label: string }[] = [
    { value: "all", label: "All" },
    { value: "velocity", label: "Velocity" },
    { value: "capacity", label: "Capacity" },
    { value: "topics", label: "Topics" },
  ];

  ngOnInit(): void {
    this.state.loadMetrics();
    this.state.loadIterations();
    this.state.loadGroupedIssues();
  }
}
