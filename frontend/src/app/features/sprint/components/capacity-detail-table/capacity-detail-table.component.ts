import {
  Component,
  Input,
  inject,
  OnInit,
  OnChanges,
  SimpleChanges,
  signal,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import {
  CapacityGrid,
  TeamMember,
  SprintDetails,
} from "../../models/capacity.model";
import { CapacityStateService } from "../../services/capacity-state.service";
import {
  calculateEffectiveDays,
  isValidDaysOff,
  isValidTimeOverride,
  clampDaysOff,
  clampTimeOverride,
} from "../../utils/capacity-calculations";

interface DetailRow {
  sprintName: string;
  sprintLabel: string;
  pi: string;
  iteration: number;
  type: "IT" | "IP";
  days: number;
  memberId: string;
  memberName: string;
  timeOverride: number;
  daysOff: number;
  effectiveDays: number;
  excludedFromCapacity: boolean;
}

@Component({
  selector: "app-capacity-detail-table",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./capacity-detail-table.component.html",
  styleUrls: ["./capacity-detail-table.component.scss"],
})
export class CapacityDetailTableComponent implements OnInit, OnChanges {
  @Input() grid!: CapacityGrid;

  readonly capState = inject(CapacityStateService);

  rows: DetailRow[] = [];
  filterSprints = new Set<string>();
  filterMembers = new Set<string>();
  editingCellKey: string | null = null;
  editingValue: string = "";
  validationError: string | null = null;

  // Dropdown state
  sprintDropdownOpen = signal(false);
  memberDropdownOpen = signal(false);

  get availableSprints(): { key: string; label: string }[] {
    const seen = new Set<string>();
    const result: { key: string; label: string }[] = [];
    for (const row of this.rows) {
      if (!seen.has(row.sprintName)) {
        seen.add(row.sprintName);
        result.push({ key: row.sprintName, label: row.sprintLabel });
      }
    }
    return result;
  }

  get availableMembers(): { id: string; name: string }[] {
    const seen = new Set<string>();
    const result: { id: string; name: string }[] = [];
    for (const row of this.rows) {
      if (!seen.has(row.memberId)) {
        seen.add(row.memberId);
        result.push({ id: row.memberId, name: row.memberName });
      }
    }
    return result;
  }

  get filteredRows(): DetailRow[] {
    return this.rows.filter(
      (row) =>
        (this.filterSprints.size === 0 ||
          this.filterSprints.has(row.sprintName)) &&
        (this.filterMembers.size === 0 || this.filterMembers.has(row.memberId)),
    );
  }

  resetFilters(): void {
    this.filterSprints.clear();
    this.filterMembers.clear();
  }

  ngOnInit(): void {
    this.buildRows();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["grid"]) {
      this.buildRows();
    }
  }

  private buildRows(): void {
    if (!this.grid) return;

    this.rows = [];
    const { members, sprints, daysPerSprint, daysOffGrid, sprintDetails } =
      this.grid;

    for (const sprintName of sprints) {
      const details = sprintDetails[sprintName];
      if (!details) continue;

      const type = details.ip ? "IP" : "IT";
      const days = daysPerSprint[sprintName];

      for (const member of members) {
        const excluded = member.excludedFromCapacity === true;
        const daysOff = daysOffGrid[member.id]?.[sprintName] ?? 0;
        const effectiveDays = excluded
          ? 0
          : calculateEffectiveDays(days, daysOff, member.timeOverride);

        this.rows.push({
          sprintName,
          sprintLabel: `PI ${details.pi} – ${type} ${details.iteration}`,
          pi: details.pi,
          iteration: details.iteration,
          type,
          days,
          memberId: member.id,
          memberName: member.name,
          timeOverride: member.timeOverride,
          daysOff,
          effectiveDays,
          excludedFromCapacity: excluded,
        });
      }
    }
  }

  toggleSprintDropdown(): void {
    this.sprintDropdownOpen.update((v) => !v);
    this.memberDropdownOpen.set(false);
  }

  toggleMemberDropdown(): void {
    this.memberDropdownOpen.update((v) => !v);
    this.sprintDropdownOpen.set(false);
  }

  selectSprint(sprintKey: string): void {
    if (this.filterSprints.has(sprintKey)) {
      this.filterSprints.delete(sprintKey);
    } else {
      this.filterSprints.add(sprintKey);
    }
  }

  selectMember(memberId: string): void {
    if (this.filterMembers.has(memberId)) {
      this.filterMembers.delete(memberId);
    } else {
      this.filterMembers.add(memberId);
    }
  }

  isSprintSelected(sprintKey: string): boolean {
    return this.filterSprints.has(sprintKey);
  }

  isMemberSelected(memberId: string): boolean {
    return this.filterMembers.has(memberId);
  }

  trackRow(_: number, row: DetailRow): string {
    return `${row.pi}-${row.iteration}-${row.memberId}`;
  }

  getCellKey(row: DetailRow, field: string): string {
    return `${row.memberId}:${row.pi}-${row.iteration}:${field}`;
  }

  isEditingCell(cellKey: string): boolean {
    return this.editingCellKey === cellKey;
  }

  startEditTimeOverride(row: DetailRow): void {
    const cellKey = this.getCellKey(row, "timeOverride");
    this.editingCellKey = cellKey;
    this.editingValue = row.timeOverride.toString();
    this.validationError = null;
  }

  startEditDaysOff(row: DetailRow): void {
    const cellKey = this.getCellKey(row, "daysOff");
    this.editingCellKey = cellKey;
    this.editingValue = row.daysOff.toString();
    this.validationError = null;
  }

  cancelEdit(): void {
    this.editingCellKey = null;
    this.validationError = null;
  }

  saveDaysOff(row: DetailRow, field: string): void {
    const value = parseFloat(this.editingValue);

    if (field === "daysOff") {
      if (!isValidDaysOff(value)) {
        this.validationError = "Days off must be >= 0";
        return;
      }
      const clamped = clampDaysOff(value, row.days);
      this.capState.updateDaysOff(
        row.memberId,
        this.getSprintName(row),
        clamped,
      );
    } else if (field === "timeOverride") {
      if (!isValidTimeOverride(value)) {
        this.validationError = "Time override must be between 0 and 1";
        return;
      }
      const clamped = clampTimeOverride(value);
      this.capState.updateTimeOverride(row.memberId, clamped);
    }

    this.editingCellKey = null;
    this.validationError = null;
  }

  private getSprintName(row: DetailRow): string {
    return row.sprintName;
  }

  getValidationErrorClass(): string {
    return this.validationError ? "validation-error" : "";
  }
}
