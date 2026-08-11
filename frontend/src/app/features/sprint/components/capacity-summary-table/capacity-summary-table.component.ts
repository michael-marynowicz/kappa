import {
  Component,
  Input,
  OnInit,
  OnChanges,
  SimpleChanges,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { CapacityGrid, SprintDetails } from "../../models/capacity.model";
import {
  calculateEffectiveDays,
  calculate80Percent,
  calculateSP,
} from "../../utils/capacity-calculations";

interface SummaryRow {
  pi: string;
  iteration: number;
  type: "IT" | "IP";
  sprintName: string;
  total: number;
  percent80: number;
  sp: number;
  isIPWeek: boolean;
}

@Component({
  selector: "app-capacity-summary-table",
  standalone: true,
  imports: [CommonModule],
  templateUrl: "./capacity-summary-table.component.html",
  styleUrls: ["./capacity-summary-table.component.scss"],
})
export class CapacitySummaryTableComponent implements OnInit, OnChanges {
  @Input() grid!: CapacityGrid;

  rows: SummaryRow[] = [];
  totalRow: SummaryRow | null = null;
  totalWithoutIPRow: SummaryRow | null = null;

  ngOnInit(): void {
    this.buildSummary();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["grid"]) {
      this.buildSummary();
    }
  }

  private buildSummary(): void {
    if (!this.grid) return;

    this.rows = [];
    const { members, sprints, daysPerSprint, daysOffGrid, sprintDetails } =
      this.grid;

    let grandTotal = 0;
    let grandTotal80 = 0;
    let grandTotalWithoutIP = 0;
    let grandTotal80WithoutIP = 0;

    for (const sprintName of sprints) {
      const details = sprintDetails[sprintName];
      if (!details) continue;

      const type = details.ip ? "IP" : "IT";
      const days = daysPerSprint[sprintName];

      // Calculate total for this sprint
      let total = 0;
      for (const member of members) {
        const daysOff = daysOffGrid[member.id]?.[sprintName] ?? 0;
        total += calculateEffectiveDays(days, daysOff, member.timeOverride);
      }

      const percent80 = calculate80Percent(total);
      const sp = calculateSP(percent80);

      this.rows.push({
        pi: details.pi,
        iteration: details.iteration,
        type,
        sprintName,
        total,
        percent80,
        sp,
        isIPWeek: details.ip,
      });

      grandTotal += total;
      grandTotal80 += percent80;

      if (!details.ip) {
        grandTotalWithoutIP += total;
        grandTotal80WithoutIP += percent80;
      }
    }

    // Total row (all sprints)
    this.totalRow = {
      pi: "TOTAL",
      iteration: 0,
      type: "IT",
      sprintName: "TOTAL",
      total: grandTotal,
      percent80: grandTotal80,
      sp: calculateSP(grandTotal80),
      isIPWeek: false,
    };

    // Total without IP week
    this.totalWithoutIPRow = {
      pi: "TOTAL",
      iteration: 0,
      type: "IT",
      sprintName: "TOTAL (without IP)",
      total: grandTotalWithoutIP,
      percent80: grandTotal80WithoutIP,
      sp: calculateSP(grandTotal80WithoutIP),
      isIPWeek: false,
    };
  }

  trackRow(_: number, row: SummaryRow): string {
    return row.sprintName;
  }
}
