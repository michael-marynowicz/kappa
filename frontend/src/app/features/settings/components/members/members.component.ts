import {
  Component,
  computed,
  HostListener,
  inject,
  OnInit,
  signal,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { RouterLink } from "@angular/router";
import { TranslatePipe } from "../../../../shared/pipes/translate.pipe";
import { OrganizationApiService } from "../../../../core/services/organization-api.service";
import { InvitationApiService } from "../../../../core/services/invitation-api.service";
import { SubscriptionStateService } from "../../../../core/services/subscription-state.service";
import { AuthStateService } from "../../../../core/services/auth-state.service";
import { JiraConfigApiService } from "../../../../core/services/jira-config-api.service";
import { JiraDashboard } from "../../../../core/models/jira-config.model";
import { OrganizationMember } from "../../../../core/models/organization.model";
import {
  Invitation,
  BulkInviteResult,
} from "../../../../core/models/invitation.model";
import { HttpErrorResponse } from "@angular/common/http";
import { I18nService } from "../../../../i18n/i18n.service";

@Component({
  selector: "app-members",
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, TranslatePipe],
  templateUrl: "./members.component.html",
  styleUrls: ["./members.component.scss"],
})
export class MembersComponent implements OnInit {
  private readonly orgApi = inject(OrganizationApiService);
  private readonly invitationApi = inject(InvitationApiService);
  private readonly jiraApi = inject(JiraConfigApiService);
  readonly subState = inject(SubscriptionStateService);
  readonly authState = inject(AuthStateService);
  private readonly i18n = inject(I18nService);

  readonly members = signal<OrganizationMember[]>([]);
  readonly invitations = signal<Invitation[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);

  // ── Team filter ──────────────────────────────────────────
  readonly activeDashboardFilter = signal<string | null>(null);

  readonly filteredMembers = computed(() => {
    const filter = this.activeDashboardFilter();
    if (!filter) return this.members();
    return this.members().filter((m) => {
      const ids = m.dashboardIds ?? [];
      // Members without assigned dashboards are visible in all filters
      return ids.length === 0 || ids.includes(filter);
    });
  });

  readonly filteredInvitations = computed(() => {
    const filter = this.activeDashboardFilter();
    if (!filter) return this.invitations();
    return this.invitations().filter((i) => {
      const ids = i.dashboards?.map((d) => d.id) ?? i.dashboardIds ?? [];
      return ids.length === 0 || ids.includes(filter);
    });
  });

  // ── Single invite ──────────────────────────────────────────────
  inviteEmail = "";
  inviteRole = "MEMBER";
  readonly inviting = signal(false);
  readonly inviteRoleDropdownOpen = signal(false);

  // ── Dashboard picker (shared between single + bulk) ────────────
  readonly availableDashboards = signal<JiraDashboard[]>([]);
  readonly inviteSelectedDashboards = signal<string[]>([]);
  readonly inviteDashboardDropdownOpen = signal(false);
  readonly bulkSelectedDashboards = signal<string[]>([]);
  readonly bulkDashboardDropdownOpen = signal(false);

  // ── Bulk invite ────────────────────────────────────────────────
  readonly BULK_MAX = 2000;
  bulkEmailInput = "";
  bulkRole = "MEMBER";
  bulkEmails = signal<string[]>([]);
  bulkInviting = signal(false);
  bulkResult = signal<BulkInviteResult | null>(null);
  readonly bulkRoleDropdownOpen = signal(false);

  private readonly emailRegex =
    /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

  formatLimit(value: number | null): string {
    return value === null ? this.i18n.t("common.unlimited") : String(value);
  }

  private get currentPlan() {
    const plans = this.subState.plans();
    const sub = this.subState.subscription();
    if (!sub || !plans.length) return null;
    return (
      plans.find((p) => p.code.toLowerCase() === sub.planCode.toLowerCase()) ??
      null
    );
  }

  get maxMembers(): number | null {
    return this.currentPlan?.maxUsers ?? null;
  }

  get totalCount(): number {
    return this.members().length + this.invitations().length;
  }

  get planName(): string {
    const sub = this.subState.subscription();
    return sub?.planName ?? "Free";
  }

  ngOnInit(): void {
    this.loadData();
    if (!this.subState.subscription()) this.subState.loadSubscription();
    if (!this.subState.plans().length) this.subState.loadPlans();
    this.jiraApi.listDashboards().subscribe({
      next: (dashboards) => {
        this.availableDashboards.set(dashboards);
        // Pre-select active dashboard as default filter
        const active = dashboards.find((d) => d.active);
        if (active) {
          this.switchTeamFilter(active.id);
        }
      },
      error: () => {},
    });
  }

  loadData(): void {
    this.loading.set(true);
    this.error.set(null);

    this.orgApi.getMembers().subscribe({
      next: (members) => {
        this.members.set(members);
        this.loading.set(false);
      },
      error: () => {
        this.error.set("members.error.load");
        this.loading.set(false);
      },
    });

    this.invitationApi.getInvitations().subscribe({
      next: (invitations) => this.invitations.set(invitations),
      error: () => {},
    });
  }

  onInvite(): void {
    const email = this.inviteEmail.trim();
    if (!email) return;
    if (!this.emailRegex.test(email)) {
      this.error.set("members.error.invalid_email");
      return;
    }

    this.inviting.set(true);
    this.error.set(null);
    this.success.set(null);

    this.invitationApi
      .createInvitation({
        email,
        role: this.inviteRole,
        dashboardIds:
          this.inviteSelectedDashboards().length > 0
            ? this.inviteSelectedDashboards()
            : undefined,
      })
      .subscribe({
        next: (invitation) => {
          this.invitations.update((list) => [
            ...list,
            invitation || {
              id: "",
              email,
              role: this.inviteRole,
              status: "PENDING",
              createdAt: new Date().toISOString(),
            },
          ]);
          this.inviteEmail = "";
          this.inviteSelectedDashboards.set([]);
          this.inviting.set(false);
          this.success.set(
            this.i18n.tWithParams("members.success.invited", { email }),
          );
          setTimeout(() => this.success.set(null), 4000);
        },
        error: (err: HttpErrorResponse) => {
          this.inviting.set(false);
          if (err.status === 402) {
            this.error.set("members.error.member_limit");
          } else if (err.status === 409) {
            this.error.set("members.error.invite_conflict");
          } else {
            this.error.set(err.error?.message ?? "members.error.invite_failed");
          }
        },
      });
  }

  switchTeamFilter(id: string | null): void {
    this.activeDashboardFilter.set(id);
    const preselect = id ? [id] : [];
    this.inviteSelectedDashboards.set(preselect);
    this.bulkSelectedDashboards.set(preselect);
  }

  toggleInviteRoleDropdown(): void {
    if (this.inviting()) return;
    this.inviteRoleDropdownOpen.update((v) => !v);
    this.bulkRoleDropdownOpen.set(false);
    this.inviteDashboardDropdownOpen.set(false);
    this.bulkDashboardDropdownOpen.set(false);
  }

  selectInviteRole(role: string): void {
    this.inviteRole = role;
    this.inviteRoleDropdownOpen.set(false);
  }

  // ── Bulk invite logic ──────────────────────────────────────────

  onBulkInputChange(): void {
    const parsed = this.parseEmails(this.bulkEmailInput);
    this.bulkEmails.set(parsed);
    this.bulkResult.set(null);
  }

  removeEmail(email: string): void {
    this.bulkEmails.update((list) => list.filter((e) => e !== email));
    // Sync textarea to reflect removal
    this.bulkEmailInput = this.bulkEmails().join("\n");
  }

  onBulkInvite(): void {
    const emails = this.bulkEmails();
    if (!emails.length) return;

    this.bulkInviting.set(true);
    this.error.set(null);
    this.bulkResult.set(null);

    this.invitationApi
      .bulkInvite({
        emails,
        role: this.bulkRole,
        dashboardIds:
          this.bulkSelectedDashboards().length > 0
            ? this.bulkSelectedDashboards()
            : undefined,
      })
      .subscribe({
        next: (result) => {
          this.bulkResult.set(result);
          this.bulkEmails.set([]);
          this.bulkEmailInput = "";
          this.bulkSelectedDashboards.set([]);
          this.bulkInviting.set(false);
          // Refresh pending list to show newly invited
          this.invitationApi.getInvitations().subscribe({
            next: (inv) => this.invitations.set(inv),
            error: () => {},
          });
        },
        error: (err) => {
          if (err.status === 402) {
            this.error.set("members.error.member_limit");
          } else {
            this.error.set(err.message ?? "members.error.bulk_failed");
          }
          this.bulkInviting.set(false);
        },
      });
  }

  toggleBulkRoleDropdown(): void {
    if (this.bulkInviting()) return;
    this.bulkRoleDropdownOpen.update((v) => !v);
    this.inviteRoleDropdownOpen.set(false);
    this.inviteDashboardDropdownOpen.set(false);
    this.bulkDashboardDropdownOpen.set(false);
  }

  selectBulkRole(role: string): void {
    this.bulkRole = role;
    this.bulkRoleDropdownOpen.set(false);
  }

  // ── Dashboard picker helpers ───────────────────────────────────

  toggleInviteDashboardDropdown(): void {
    if (this.inviting()) return;
    this.inviteDashboardDropdownOpen.update((v) => !v);
    this.inviteRoleDropdownOpen.set(false);
    this.bulkDashboardDropdownOpen.set(false);
  }

  toggleBulkDashboardDropdown(): void {
    if (this.bulkInviting()) return;
    this.bulkDashboardDropdownOpen.update((v) => !v);
    this.bulkRoleDropdownOpen.set(false);
    this.inviteDashboardDropdownOpen.set(false);
  }

  toggleInviteDashboard(id: string): void {
    this.inviteSelectedDashboards.update((list) =>
      list.includes(id) ? list.filter((d) => d !== id) : [...list, id],
    );
  }

  toggleBulkDashboard(id: string): void {
    this.bulkSelectedDashboards.update((list) =>
      list.includes(id) ? list.filter((d) => d !== id) : [...list, id],
    );
  }

  dashboardName(id: string): string {
    return this.availableDashboards().find((d) => d.id === id)?.name ?? id;
  }

  clearBulkResult(): void {
    this.bulkResult.set(null);
  }

  private parseEmails(raw: string): string[] {
    const seen = new Set<string>();
    return raw
      .split(/[\n,;]+/)
      .map((e) => e.trim().toLowerCase())
      .filter((e) => {
        if (!e || !this.emailRegex.test(e) || seen.has(e)) return false;
        seen.add(e);
        return true;
      })
      .slice(0, this.BULK_MAX);
  }

  // ── Shared ─────────────────────────────────────────────────────

  onRevoke(invitation: Invitation): void {
    this.invitationApi.revokeInvitation(invitation.id).subscribe({
      next: () => {
        this.invitations.update((list) =>
          list.filter((i) => i.id !== invitation.id),
        );
      },
      error: () => {
        this.error.set("Failed to revoke invitation.");
      },
    });
  }

  onRemoveMember(member: OrganizationMember): void {
    this.orgApi.removeMember(member.id).subscribe({
      next: () => {
        this.members.update((list) => list.filter((m) => m.id !== member.id));
      },
      error: () => {
        this.error.set("Failed to remove member.");
      },
    });
  }

  clearError(): void {
    this.error.set(null);
  }

  isCurrentUser(member: OrganizationMember): boolean {
    return member.id === this.authState.user()?.id;
  }

  get isAdmin(): boolean {
    return this.authState.user()?.role === "ADMIN";
  }

  get availableRoles(): string[] {
    return this.isAdmin ? ["ADMIN", "MEMBER", "VIEWER"] : ["MEMBER", "VIEWER"];
  }

  @HostListener("document:click", ["$event"])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement | null;
    if (!target?.closest(".mr-dropdown") && !target?.closest(".db-picker")) {
      this.inviteRoleDropdownOpen.set(false);
      this.bulkRoleDropdownOpen.set(false);
      this.inviteDashboardDropdownOpen.set(false);
      this.bulkDashboardDropdownOpen.set(false);
    }
  }
}
