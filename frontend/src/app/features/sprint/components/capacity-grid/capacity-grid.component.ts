import { Component, Input, inject, OnInit, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { CapacityStateService } from "../../services/capacity-state.service";
import { MemberRole, TeamMember } from "../../models/capacity.model";
import { PremiumOverlayComponent } from "../../../../shared/components/premium-overlay/premium-overlay.component";
import { CapacityDetailTableComponent } from "../capacity-detail-table/capacity-detail-table.component";
import { CapacitySummaryTableComponent } from "../capacity-summary-table/capacity-summary-table.component";
import { TranslatePipe } from "../../../../shared/pipes/translate.pipe";

@Component({
  selector: "app-capacity-grid",
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    PremiumOverlayComponent,
    CapacityDetailTableComponent,
    CapacitySummaryTableComponent,
    TranslatePipe,
  ],
  templateUrl: "./capacity-grid.component.html",
  styleUrls: ["./capacity-grid.component.scss"],
})
export class CapacityGridComponent implements OnInit {
  readonly capState = inject(CapacityStateService);

  /**
   * Whether this component should render its own error banner.
   * Set to `false` when an ancestor page already shows a page-level
   * error banner for the same CapacityStateService.error() signal
   * (e.g. the sprint dashboard's "Capacity" tab), to avoid showing
   * the same error message twice on screen.
   */
  @Input() showError = true;

  newMemberName = "";
  newMemberRole: MemberRole = "DEV";

  editingMemberId: string | null = null;
  editName = "";
  editRole: MemberRole = "DEV";

  readonly roles: MemberRole[] = ["DEV", "PDA", "QA", "SM"];
  addRoleDropdownOpen = signal(false);
  editRoleDropdownOpen = signal(false);

  toggleAddRoleDropdown(): void {
    this.addRoleDropdownOpen.update((v) => !v);
  }
  selectAddRole(role: MemberRole): void {
    this.newMemberRole = role;
    this.addRoleDropdownOpen.set(false);
  }
  toggleEditRoleDropdown(): void {
    this.editRoleDropdownOpen.update((v) => !v);
  }
  selectEditRole(role: MemberRole): void {
    this.editRole = role;
    this.editRoleDropdownOpen.set(false);
  }

  membersExpanded = true;
  activeTab: "team" | "capacity" | "summary" = "team";

  ngOnInit(): void {
    this.capState.loadGrid();
  }

  trackMember(_: number, m: TeamMember): string {
    return m.id;
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
