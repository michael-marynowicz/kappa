import { Component, inject, OnInit, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { OrganizationApiService } from "../../../../core/services/organization-api.service";
import { InvitationApiService } from "../../../../core/services/invitation-api.service";
import { SubscriptionStateService } from "../../../../core/services/subscription-state.service";
import { AuthStateService } from "../../../../core/services/auth-state.service";
import { OrganizationMember } from "../../../../core/models/organization.model";
import { Invitation } from "../../../../core/models/invitation.model";
import { HttpErrorResponse } from "@angular/common/http";

@Component({
  selector: "app-members",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./members.component.html",
  styleUrls: ["./members.component.scss"],
})
export class MembersComponent implements OnInit {
  private readonly orgApi = inject(OrganizationApiService);
  private readonly invitationApi = inject(InvitationApiService);
  readonly subState = inject(SubscriptionStateService);
  readonly authState = inject(AuthStateService);

  readonly members = signal<OrganizationMember[]>([]);
  readonly invitations = signal<Invitation[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);

  inviteEmail = "";
  inviteRole = "MEMBER";
  inviting = false;

  get maxMembers(): number | null {
    const plans = this.subState.plans();
    const sub = this.subState.subscription();
    if (!sub || !plans.length) return null;
    const currentPlan = plans.find((p) => p.code === sub.planCode);
    return currentPlan?.maxUsers ?? null;
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
    this.subState.loadSubscription();
    this.subState.loadPlans();
  }

  loadData(): void {
    this.loading.set(true);
    this.error.set(null);

    this.orgApi.getMembers().subscribe({
      next: (members) => {
        this.members.set(members);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set("Failed to load members");
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

    this.inviting = true;
    this.error.set(null);
    this.success.set(null);

    this.invitationApi
      .createInvitation({ email, role: this.inviteRole })
      .subscribe({
        next: (invitation) => {
          this.invitations.update((list) => [...list, invitation]);
          this.inviteEmail = "";
          this.inviting = false;
          this.success.set(`Invitation sent to ${email}`);
          setTimeout(() => this.success.set(null), 4000);
        },
        error: (err: HttpErrorResponse) => {
          this.inviting = false;
          if (err.status === 402) {
            this.error.set("Member limit reached. Please upgrade your plan.");
          } else if (err.status === 409) {
            this.error.set(
              "This email is already invited or already a member.",
            );
          } else {
            this.error.set(err.error?.message ?? "Failed to send invitation.");
          }
        },
      });
  }

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
}
