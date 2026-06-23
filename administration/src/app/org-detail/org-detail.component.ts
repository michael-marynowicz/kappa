import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ActivatedRoute, Router } from "@angular/router";
import { MatTableModule } from "@angular/material/table";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { MatSnackBar, MatSnackBarModule } from "@angular/material/snack-bar";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatDividerModule } from "@angular/material/divider";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatCardModule } from "@angular/material/card";
import { MatTabsModule } from "@angular/material/tabs";

import {
  OrganizationService,
  OrgAdminDetail,
  OrgMember,
  OrgInvitation,
} from "../services/organization.service";
import { OrganizationAdminService } from "../services/organization-admin.service";
import { SubscriptionAdminService } from "../services/subscription-admin.service";
import { ConfirmDialogComponent } from "../dashboard/confirm-dialog.component";
import { AssignEnterpriseDialogComponent } from "../dashboard/assign-enterprise-dialog.component";
import { AssignPilotDialogComponent } from "../dashboard/assign-pilot-dialog.component";
import { InviteUsersDialogComponent } from "./invite-users-dialog.component";
import { ExtendPilotDialogComponent } from "./extend-pilot-dialog.component";

@Component({
  selector: "app-org-detail",
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatToolbarModule,
    MatDividerModule,
    MatTooltipModule,
    MatCardModule,
    MatTabsModule,
  ],
  templateUrl: "./org-detail.component.html",
})
export class OrgDetailComponent implements OnInit {
  orgId!: string;
  detail: OrgAdminDetail | null = null;
  members: OrgMember[] = [];
  invitations: OrgInvitation[] = [];

  detailLoading = true;
  membersLoading = true;
  invitationsLoading = true;
  actionLoading = false;

  memberColumns = ["name", "email", "role", "lastLogin", "status", "actions"];
  invitationColumns = ["email", "role", "createdAt", "actions"];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orgService: OrganizationService,
    private orgAdminService: OrganizationAdminService,
    private subAdminService: SubscriptionAdminService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.orgId = this.route.snapshot.paramMap.get("id")!;
    this.loadDetail();
    this.loadMembers();
    this.loadInvitations();
  }

  goBack(): void {
    this.router.navigate(["/dashboard"]);
  }

  // ── Data loading ─────────────────────────────────────────────────────────────

  loadDetail(): void {
    this.detailLoading = true;
    this.orgService.getAdminOrganizationDetail(this.orgId).subscribe({
      next: (d) => {
        this.detail = d;
        this.detailLoading = false;
      },
      error: () => {
        this.detailLoading = false;
      },
    });
  }

  loadMembers(): void {
    this.membersLoading = true;
    this.orgAdminService.getMembers(this.orgId).subscribe({
      next: (m) => {
        this.members = m;
        this.membersLoading = false;
      },
      error: () => {
        this.membersLoading = false;
      },
    });
  }

  loadInvitations(): void {
    this.invitationsLoading = true;
    this.orgAdminService.getInvitations(this.orgId).subscribe({
      next: (i) => {
        this.invitations = i;
        this.invitationsLoading = false;
      },
      error: () => {
        this.invitationsLoading = false;
      },
    });
  }

  // ── Formatting helpers ────────────────────────────────────────────────────────

  getOrgStatusLabel(): string {
    if (!this.detail) return "—";
    if (!this.detail.active) return "SUSPENDED";
    if (this.detail.subscription?.subscriptionType === "PILOT") return "PILOT";
    return "ACTIVE";
  }

  getOrgStatusClass(): string {
    if (!this.detail?.active) return "chip-red";
    if (this.detail?.subscription?.subscriptionType === "PILOT")
      return "chip-orange";
    return "chip-green";
  }

  getMemberStatusClass(status: string): string {
    return status === "ACTIVE" ? "chip-green" : "chip-grey";
  }

  formatDate(d: string | null | undefined): string {
    if (!d) return "—";
    return new Date(d).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  }

  formatDateTime(d: string | null | undefined): string {
    if (!d) return "—";
    return new Date(d).toLocaleString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  // ── Member actions ────────────────────────────────────────────────────────────

  deactivateMember(member: OrgMember): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: "420px",
      data: {
        title: "Deactivate user",
        message: `Deactivate ${member.name} (${member.email})?`,
      },
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.orgAdminService.deactivateMember(this.orgId, member.id).subscribe({
          next: () => {
            this.snackBar.open("User deactivated.", "Close", {
              duration: 3000,
            });
            this.loadMembers();
          },
        });
      }
    });
  }

  reactivateMember(member: OrgMember): void {
    this.orgAdminService.reactivateMember(this.orgId, member.id).subscribe({
      next: () => {
        this.snackBar.open("User reactivated.", "Close", { duration: 3000 });
        this.loadMembers();
      },
    });
  }

  removeMember(member: OrgMember): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: "420px",
      data: {
        title: "Remove user",
        message: `Permanently remove ${member.name} (${member.email}) from this organization?`,
      },
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.orgAdminService.removeMember(this.orgId, member.id).subscribe({
          next: () => {
            this.snackBar.open("User removed.", "Close", { duration: 3000 });
            this.loadMembers();
            this.loadDetail();
          },
        });
      }
    });
  }

  // ── Invitation actions ────────────────────────────────────────────────────────

  openInviteUsers(): void {
    const ref = this.dialog.open(InviteUsersDialogComponent, {
      width: "520px",
      data: { orgName: this.detail?.name },
    });
    ref.afterClosed().subscribe((result) => {
      if (result) {
        this.orgAdminService
          .inviteUsers(this.orgId, result.emails, result.role)
          .subscribe({
            next: () => {
              this.snackBar.open(
                `${result.emails.length} invitation(s) sent.`,
                "Close",
                { duration: 3000 },
              );
              this.loadInvitations();
              this.loadDetail();
            },
          });
      }
    });
  }

  resendInvitation(inv: OrgInvitation): void {
    this.orgAdminService.resendInvitation(this.orgId, inv.id).subscribe({
      next: () => {
        this.snackBar.open(`Invitation resent to ${inv.email}.`, "Close", {
          duration: 3000,
        });
      },
    });
  }

  revokeInvitation(inv: OrgInvitation): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: "420px",
      data: {
        title: "Revoke invitation",
        message: `Revoke invitation for ${inv.email}?`,
      },
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.orgAdminService.revokeInvitation(this.orgId, inv.id).subscribe({
          next: () => {
            this.snackBar.open("Invitation revoked.", "Close", {
              duration: 3000,
            });
            this.loadInvitations();
            this.loadDetail();
          },
        });
      }
    });
  }

  // ── Organization actions ──────────────────────────────────────────────────────

  activateOrg(): void {
    this.actionLoading = true;
    this.subAdminService.setActivation(this.orgId, true).subscribe({
      next: () => {
        this.actionLoading = false;
        this.snackBar.open("Organization activated!", "Close", {
          duration: 3000,
        });
        this.loadDetail();
      },
      error: () => {
        this.actionLoading = false;
      },
    });
  }

  suspendOrg(): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: "420px",
      data: {
        title: "Suspend organization",
        message: `Suspend "${this.detail?.name}"? All users will lose access.`,
      },
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.actionLoading = true;
        this.subAdminService.setActivation(this.orgId, false).subscribe({
          next: () => {
            this.actionLoading = false;
            this.snackBar.open("Organization suspended.", "Close", {
              duration: 3000,
            });
            this.loadDetail();
          },
          error: () => {
            this.actionLoading = false;
          },
        });
      }
    });
  }

  convertToEnterprise(): void {
    const ref = this.dialog.open(AssignEnterpriseDialogComponent, {
      width: "420px",
      data: { orgName: this.detail?.name },
    });
    ref.afterClosed().subscribe((result) => {
      if (result) {
        this.actionLoading = true;
        this.subAdminService
          .assignEnterprise(this.orgId, result.planCode)
          .subscribe({
            next: () => {
              this.actionLoading = false;
              this.snackBar.open("Enterprise plan assigned!", "Close", {
                duration: 3000,
              });
              this.loadDetail();
            },
            error: () => {
              this.actionLoading = false;
            },
          });
      }
    });
  }

  convertToPilot(): void {
    const ref = this.dialog.open(AssignPilotDialogComponent, {
      width: "420px",
      data: { orgName: this.detail?.name },
    });
    ref.afterClosed().subscribe((result) => {
      if (result) {
        this.actionLoading = true;
        this.subAdminService
          .assignPilot(this.orgId, result.planCode, result.pilotExpiresAt)
          .subscribe({
            next: () => {
              this.actionLoading = false;
              this.snackBar.open("Pilot plan assigned!", "Close", {
                duration: 3000,
              });
              this.loadDetail();
            },
            error: () => {
              this.actionLoading = false;
            },
          });
      }
    });
  }

  extendPilot(): void {
    const ref = this.dialog.open(ExtendPilotDialogComponent, {
      width: "420px",
      data: {
        orgName: this.detail?.name,
        currentExpiry: this.detail?.subscription?.pilotExpiresAt,
      },
    });
    ref.afterClosed().subscribe((result) => {
      if (result) {
        this.actionLoading = true;
        const planCode = this.detail?.subscription?.planCode ?? "BUSINESS";
        this.subAdminService
          .assignPilot(this.orgId, planCode, result.pilotExpiresAt)
          .subscribe({
            next: () => {
              this.actionLoading = false;
              this.snackBar.open("Pilot expiration extended!", "Close", {
                duration: 3000,
              });
              this.loadDetail();
            },
            error: () => {
              this.actionLoading = false;
            },
          });
      }
    });
  }

  convertToSelfService(): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: "420px",
      data: {
        title: "Convert to Self-service",
        message:
          "This will remove Enterprise/Pilot status and prepare the organization for Stripe checkout.",
      },
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.actionLoading = true;
        this.subAdminService.convertToSelfService(this.orgId).subscribe({
          next: () => {
            this.actionLoading = false;
            this.snackBar.open("Converted to self-service!", "Close", {
              duration: 3000,
            });
            this.loadDetail();
          },
          error: () => {
            this.actionLoading = false;
          },
        });
      }
    });
  }

  deleteOrg(): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: "440px",
      data: {
        title: "Delete organization",
        message: `Permanently delete "${this.detail?.name}"? This cannot be undone. All users, data and subscriptions will be removed.`,
      },
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.actionLoading = true;
        this.orgAdminService.deleteOrganization(this.orgId).subscribe({
          next: () => {
            this.actionLoading = false;
            this.snackBar.open("Organization deleted.", "Close", {
              duration: 3000,
            });
            this.router.navigate(["/dashboard"]);
          },
          error: () => {
            this.actionLoading = false;
          },
        });
      }
    });
  }
}
