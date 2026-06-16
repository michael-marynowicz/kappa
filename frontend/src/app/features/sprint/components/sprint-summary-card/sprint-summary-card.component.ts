import { Component, Input } from "@angular/core";
import { CommonModule } from "@angular/common";
import { SprintSummary } from "../../models/sprint-issue.model";

/**
 * Presentational component: displays aggregated sprint KPIs.
 * Pure input-based; no service injection, no side effects.
 */
@Component({
  selector: "app-sprint-summary-card",
  standalone: true,
  imports: [CommonModule],
  templateUrl: "./sprint-summary-card.component.html",
  styleUrls: ["./sprint-summary-card.component.scss"],
})
export class SprintSummaryCardComponent {
  @Input({ required: true }) summary!: SprintSummary;
}
