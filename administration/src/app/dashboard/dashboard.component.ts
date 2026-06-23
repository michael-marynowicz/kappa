import { Component, OnInit, AfterViewInit, ViewChild } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { Router } from "@angular/router";
import { MatTableModule, MatTableDataSource } from "@angular/material/table";
import { MatSort, MatSortModule } from "@angular/material/sort";
import { MatPaginator, MatPaginatorModule } from "@angular/material/paginator";
import { MatButtonModule } from "@angular/material/button";
import { MatMenuModule } from "@angular/material/menu";
import { MatIconModule } from "@angular/material/icon";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { MatSnackBar, MatSnackBarModule } from "@angular/material/snack-bar";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatDividerModule } from "@angular/material/divider";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";

import { AuthService } from "../services/auth.service";
import {
  OrganizationService,
  OrgAdminSummary,
} from "../services/organization.service";
import { SubscriptionAdminService } from "../services/subscription-admin.service";
import { ConfirmDialogComponent } from "./confirm-dialog.component";

@Component({
  selector: "app-dashboard",
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatButtonModule,
    MatMenuModule,
    MatIconModule,
    MatDialogModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatToolbarModule,
    MatDividerModule,
    MatTooltipModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: "./dashboard.component.html",
})
export class DashboardComponent implements OnInit, AfterViewInit {
  displayedColumns = [
    "name",
    "status",
    "userCount",
    "pendingInvitations",
    "createdAt",
    "actions",
  ];
  dataSource = new MatTableDataSource<OrgAdminSummary>([]);
  tableLoading = true;
  searchValue = "";

  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private readonly authService: AuthService,
    private readonly orgService: OrganizationService,
    private readonly subAdminService: SubscriptionAdminService,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.dataSource.sortingDataAccessor = (row, column) => {
      switch (column) {
        case "name":
          return row.name;
        case "status":
          return this.getOrgStatusLabel(row);
        case "userCount":
          return row.userCount;
        case "pendingInvitations":
          return row.pendingInvitationCount;
        case "createdAt":
          return row.createdAt;
        default:
          return "";
      }
    };
    this.dataSource.filterPredicate = (row, filter) => {
      const s = filter.toLowerCase();
      return (
        row.name.toLowerCase().includes(s) ||
        row.email.toLowerCase().includes(s) ||
        row.slug.toLowerCase().includes(s)
      );
    };
  }

  loadData(): void {
    this.tableLoading = true;
    this.orgService.getAdminOrganizations().subscribe({
      next: (orgs) => {
        this.dataSource.data = orgs;
        this.tableLoading = false;
      },
      error: () => {
        this.tableLoading = false;
        this.snackBar.open("Failed to load organizations", "Close", {
          duration: 4000,
        });
      },
    });
  }

  applySearch(): void {
    this.dataSource.filter = this.searchValue.trim();
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  clearSearch(): void {
    this.searchValue = "";
    this.applySearch();
  }

  openDetail(row: OrgAdminSummary): void {
    this.router.navigate(["/org", row.id]);
  }

  logout(): void {
    this.authService.logout();
  }

  getOrgStatusLabel(row: OrgAdminSummary): string {
    if (!row.active) return "SUSPENDED";
    if (row.subscriptionType === "PILOT") return "PILOT";
    return "ACTIVE";
  }

  getOrgStatusClass(row: OrgAdminSummary): string {
    if (!row.active) return "chip-red";
    if (row.subscriptionType === "PILOT") return "chip-orange";
    return "chip-green";
  }

  getTypeLabel(type?: string | null): string {
    switch (type) {
      case "SELF_SERVICE":
        return "Self-service";
      case "ENTERPRISE":
        return "Enterprise";
      case "PILOT":
        return "Pilot";
      default:
        return type ?? "—";
    }
  }

  formatDate(dateStr: string | null | undefined): string {
    if (!dateStr) return "—";
    return new Date(dateStr).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  }

  stopPropagation(event: Event): void {
    event.stopPropagation();
  }

  activate(row: OrgAdminSummary, event: Event): void {
    event.stopPropagation();
    this.subAdminService.setActivation(row.id, true).subscribe({
      next: () => {
        this.snackBar.open("Organization activated!", "Close", {
          duration: 3000,
        });
        this.loadData();
      },
    });
  }

  deactivate(row: OrgAdminSummary, event: Event): void {
    event.stopPropagation();
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: "420px",
      data: {
        title: "Suspend organization",
        message: `Suspend "${row.name}"? The organization will lose access.`,
      },
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.subAdminService.setActivation(row.id, false).subscribe({
          next: () => {
            this.snackBar.open("Organization suspended.", "Close", {
              duration: 3000,
            });
            this.loadData();
          },
        });
      }
    });
  }

  // kept as dead-code guard — no longer used directly but preserves compilation
  private _unused(): void {
    // AssignEnterpriseDialogComponent, AssignPilotDialogComponent are used in org-detail
  }

  // placeholder to satisfy old call sites during migration
  openAssignEnterprise(_row: unknown): void {
    /* moved to org-detail */
  }
  openAssignPilot(_row: unknown): void {
    /* moved to org-detail */
  }
}
