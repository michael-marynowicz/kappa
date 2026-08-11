import { Injectable, inject, signal } from "@angular/core";
import { CapacityApiService } from "./capacity-api.service";
import { CapacityGrid, TeamMember, MemberRole } from "../models/capacity.model";

/** Check if an error is a feature-gated 403 from the interceptor. */
function isFeatureGatedError(err: unknown): boolean {
  return (
    typeof err === "object" &&
    err !== null &&
    "featureGated" in err &&
    (err as any).featureGated === true
  );
}

function getErrorStatus(err: unknown): number | null {
  if (
    typeof err === "object" &&
    err !== null &&
    "status" in err &&
    typeof (err as { status?: unknown }).status === "number"
  ) {
    return (err as { status: number }).status;
  }
  return null;
}

@Injectable({ providedIn: "root" })
export class CapacityStateService {
  private readonly api = inject(CapacityApiService);

  private readonly _grid = signal<CapacityGrid | null>(null);
  private readonly _loading = signal(false);
  private readonly _error = signal<string | null>(null);
  private readonly _errorStatus = signal<number | null>(null);
  private readonly _savingCell = signal<string | null>(null);
  private readonly _gated = signal(false);

  readonly grid = this._grid.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();
  readonly errorStatus = this._errorStatus.asReadonly();
  readonly savingCell = this._savingCell.asReadonly();
  readonly gated = this._gated.asReadonly();

  loadGrid(): void {
    this._loading.set(true);
    this._error.set(null);
    this._errorStatus.set(null);
    this.api.getCapacityGrid().subscribe({
      next: (g) => {
        this._grid.set(g);
        this._gated.set(false);
        this._errorStatus.set(null);
        this._loading.set(false);
      },
      error: (err) => {
        if (isFeatureGatedError(err)) {
          this._gated.set(true);
        } else {
          this._error.set(err.message ?? "Failed to load capacity");
          this._errorStatus.set(getErrorStatus(err));
        }
        this._loading.set(false);
      },
    });
  }

  addMember(name: string, role: MemberRole): void {
    this.api.addMember({ name, role }).subscribe({
      next: () => this.loadGrid(),
      error: (err) => this._error.set(err.message ?? "Failed to add member"),
    });
  }

  updateMember(member: TeamMember, name: string, role: MemberRole): void {
    this.api.updateMember(member.id, { name, role }).subscribe({
      next: () => this.loadGrid(),
      error: (err) => this._error.set(err.message ?? "Failed to update member"),
    });
  }

  deleteMember(id: string): void {
    this.api.deleteMember(id).subscribe({
      next: () => this.loadGrid(),
      error: (err) => this._error.set(err.message ?? "Failed to delete member"),
    });
  }

  updateDaysOff(memberId: string, sprintName: string, daysOff: number): void {
    const key = `${memberId}:${sprintName}`;
    this._savingCell.set(key);
    this.api
      .updateDaysOff({ teamMemberId: memberId, sprintName, daysOff })
      .subscribe({
        next: () => {
          this._grid.update((g) => {
            if (!g) return g;
            const updated = { ...g, daysOffGrid: { ...g.daysOffGrid } };
            updated.daysOffGrid[memberId] = {
              ...updated.daysOffGrid[memberId],
              [sprintName]: daysOff,
            };
            return updated;
          });
          this._savingCell.set(null);
        },
        error: (err) => {
          this._error.set(err.message ?? "Failed to update days off");
          this._savingCell.set(null);
        },
      });
  }

  updateTimeOverride(memberId: string, timeOverride: number): void {
    const key = `timeOverride:${memberId}`;
    this._savingCell.set(key);
    this.api.updateTimeOverride(memberId, timeOverride).subscribe({
      next: () => {
        this._grid.update((g) => {
          if (!g) return g;
          const updated = {
            ...g,
            members: g.members.map((m) =>
              m.id === memberId ? { ...m, timeOverride } : m,
            ),
          };
          return updated;
        });
        this._savingCell.set(null);
      },
      error: (err) => {
        this._error.set(err.message ?? "Failed to update time override");
        this._savingCell.set(null);
      },
    });
  }

  clearError(): void {
    this._error.set(null);
    this._errorStatus.set(null);
  }

  clear(): void {
    this._grid.set(null);
    this._loading.set(false);
    this._error.set(null);
    this._errorStatus.set(null);
    this._savingCell.set(null);
    this._gated.set(false);
  }

  exportXlsx(): void {
    this.api.exportXlsx().subscribe({
      next: (blob) => {
        const url = globalThis.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `capacity-report-${new Date().toISOString().split("T")[0]}.xlsx`;
        a.click();
        globalThis.URL.revokeObjectURL(url);
      },
      error: (err) => {
        if (isFeatureGatedError(err)) {
          this._gated.set(true);
        } else {
          this._error.set(err.message ?? "Failed to export capacity");
        }
      },
    });
  }
}
