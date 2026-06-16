import { Component, Input, Output, EventEmitter, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { SprintIssue } from "../../models/sprint-issue.model";
import { StatusBadgeComponent } from "../status-badge/status-badge.component";

interface EditingState {
  issueKey: string;
  value: number;
}

/**
 * Sprint Issue Table: displays all sprint issues with inline SP editing.
 *
 * Responsibilities:
 * - Render issues in a data table
 * - Allow inline editing of remaining SP per row
 * - Emit events upward — NEVER update state directly
 *
 * This is a pure presentational component with local UI state only.
 * Business state changes are emitted via EventEmitter.
 */
@Component({
  selector: "app-sprint-issue-table",
  standalone: true,
  imports: [CommonModule, FormsModule, StatusBadgeComponent],
  templateUrl: "./sprint-issue-table.component.html",
  styleUrls: ["./sprint-issue-table.component.scss"],
})
export class SprintIssueTableComponent {
  @Input({ required: true }) issues: SprintIssue[] = [];
  @Input() savingIssueKey: string | null = null;

  @Output() updateRemainingSp = new EventEmitter<{
    issueKey: string;
    remainingStoryPoints: number;
  }>();

  // Local UI-only state for the row being edited
  readonly editingState = signal<EditingState | null>(null);

  isEditing(issueKey: string): boolean {
    return this.editingState()?.issueKey === issueKey;
  }

  startEdit(issue: SprintIssue): void {
    this.editingState.set({
      issueKey: issue.issueKey,
      value: issue.remainingStoryPoints ?? issue.totalStoryPoints ?? 0,
    });
  }

  confirmEdit(issue: SprintIssue): void {
    const state = this.editingState();
    if (!state) return;

    const value = Math.max(0, Math.floor(state.value));
    this.updateRemainingSp.emit({
      issueKey: state.issueKey,
      remainingStoryPoints: value,
    });
    this.editingState.set(null);
  }

  cancelEdit(): void {
    this.editingState.set(null);
  }
}
