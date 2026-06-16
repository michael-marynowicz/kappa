import { Component, inject, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { CapacityStateService } from "../../services/capacity-state.service";
import { MemberRole, TeamMember } from "../../models/capacity.model";
import { PremiumOverlayComponent } from "../../../../shared/components/premium-overlay/premium-overlay.component";

@Component({
  selector: "app-capacity-grid",
  standalone: true,
  imports: [CommonModule, FormsModule, PremiumOverlayComponent],
  templateUrl: "./capacity-grid.component.html",
  styleUrls: ["./capacity-grid.component.scss"],
})
export class CapacityGridComponent implements OnInit {
  readonly capState = inject(CapacityStateService);

  newMemberName = "";
  newMemberRole: MemberRole = "DEV";

  editingMemberId: string | null = null;
  editName = "";
  editRole: MemberRole = "DEV";

  ngOnInit(): void {
    this.capState.loadGrid();
  }

  trackMember(_: number, m: TeamMember): string {
    return m.id;
  }

  getDaysOff(memberId: string, sprint: string): number {
    return this.capState.grid()?.daysOffGrid[memberId]?.[sprint] ?? 0;
  }

  getDaysForSprint(sprint: string): number {
    return this.capState.grid()?.daysPerSprint[sprint] ?? 0;
  }

  getTotalAvailable(memberId: string): number {
    const grid = this.capState.grid();
    if (!grid) return 0;
    return grid.sprints.reduce(
      (sum, s) => sum + this.getDaysForSprint(s) - this.getDaysOff(memberId, s),
      0,
    );
  }

  getAvailByRole(role: MemberRole, sprint: string): number {
    const grid = this.capState.grid();
    if (!grid) return 0;
    return grid.members
      .filter((m) => m.role === role)
      .reduce(
        (sum, m) =>
          sum + this.getDaysForSprint(sprint) - this.getDaysOff(m.id, sprint),
        0,
      );
  }

  getTotalByRole(role: MemberRole): number {
    const grid = this.capState.grid();
    if (!grid) return 0;
    return grid.sprints.reduce(
      (sum, s) => sum + this.getAvailByRole(role, s),
      0,
    );
  }

  getSprintTotal(sprint: string): number {
    const grid = this.capState.grid();
    if (!grid) return 0;
    return grid.members.reduce(
      (sum, m) =>
        sum + this.getDaysForSprint(sprint) - this.getDaysOff(m.id, sprint),
      0,
    );
  }

  getGrandTotal(): number {
    const grid = this.capState.grid();
    if (!grid) return 0;
    return grid.sprints.reduce((sum, s) => sum + this.getSprintTotal(s), 0);
  }

  onDaysOffChange(memberId: string, sprint: string, value: number): void {
    const grid = this.capState.grid();
    if (!grid) return;
    const clamped = Math.max(
      0,
      Math.min(grid.daysPerSprint[sprint] ?? 0, value),
    );
    this.capState.updateDaysOff(memberId, sprint, clamped);
  }

  onAddMember(): void {
    const name = this.sanitizeName(this.newMemberName);
    if (!name) return;
    this.capState.addMember(name, this.newMemberRole);
    this.newMemberName = "";
  }

  startEdit(m: TeamMember): void {
    this.editingMemberId = m.id;
    this.editName = m.name;
    this.editRole = m.role;
  }

  cancelEdit(): void {
    this.editingMemberId = null;
  }

  saveEdit(m: TeamMember): void {
    const name = this.sanitizeName(this.editName);
    if (!name) return;
    this.capState.updateMember(m, name, this.editRole);
    this.editingMemberId = null;
  }

  onDelete(m: TeamMember): void {
    if (
      !confirm(`Delete team member "${m.name}"? This action cannot be undone.`)
    ) {
      return;
    }
    this.capState.deleteMember(m.id);
  }

  /**
   * Sanitize user-provided names:
   * - Strip leading/trailing whitespace
   * - Remove HTML tags (XSS prevention)
   * - Limit to 100 characters
   */
  private sanitizeName(raw: string): string {
    return raw
      .trim()
      .replace(/<[^>]*>/g, "")
      .substring(0, 100);
  }
}
