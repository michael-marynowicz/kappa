import { Component, inject, OnInit, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { timeout } from "rxjs/operators";
import { TranslatePipe } from "../../../../shared/pipes/translate.pipe";
import { JiraConfigApiService } from "../../../../core/services/jira-config-api.service";
import { AuthStateService } from "../../../../core/services/auth-state.service";
import { SubscriptionStateService } from "../../../../core/services/subscription-state.service";
import {
  JiraAuthType,
  JiraConfig,
  JiraDashboard,
  JiraDiscoveredBoard,
} from "../../../../core/models/jira-config.model";

@Component({
  selector: "app-jira-config",
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: "./jira-config.component.html",
  styleUrls: ["./jira-config.component.scss"],
})
export class JiraConfigComponent implements OnInit {
  private readonly api = inject(JiraConfigApiService);
  private readonly authState = inject(AuthStateService);
  private readonly subState = inject(SubscriptionStateService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly config = signal<JiraConfig | null>(null);
  readonly dashboards = signal<JiraDashboard[]>([]);
  readonly discoveredBoards = signal<JiraDiscoveredBoard[]>([]);
  readonly selectedBoardId = signal<number | null>(null);
  readonly loading = signal(false);
  readonly syncing = signal(false);
  readonly discoveringBoards = signal(false);
  readonly creatingDashboard = signal(false);
  readonly dashboardsLoading = signal(false);
  readonly switchingDashboardId = signal<string | null>(null);
  readonly deletingDashboardId = signal<string | null>(null);
  readonly connecting = signal(false);
  readonly disconnecting = signal(false);
  readonly configuringServer = signal(false);
  readonly editingCredentials = signal(false);
  readonly credentialsVerified = signal(false);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);
  private successTimer: ReturnType<typeof setTimeout> | null = null;

  private showSuccess(msg: string): void {
    if (this.successTimer) {
      clearTimeout(this.successTimer);
    }
    this.success.set(msg);
    this.successTimer = setTimeout(() => this.success.set(null), 5000);
  }

  newDashboard = {
    name: "",
    projectKey: "",
    boardId: null as number | null,
  };

  form = {
    baseUrl: "https://jira.amadeus.com/agile",
    authType: "BASIC" as JiraAuthType,
    userEmail: "",
    token: "",
  };

  ngOnInit(): void {
    this.handleOAuthRedirectResult();
    this.loadConfig();
    this.loadDashboards();
    if (!this.subState.subscription()) this.subState.loadSubscription();
    if (!this.subState.plans().length) this.subState.loadPlans();
  }

  formatLimit(value: number | null): string {
    return value === null ? "Unlimited" : String(value);
  }

  private get dashboardLimitLabel(): string | null {
    const sub = this.subState.subscription();
    const plans = this.subState.plans();
    if (!sub || !plans.length) return null;
    const currentPlan = plans.find(
      (p) => p.code.toLowerCase() === sub.planCode.toLowerCase(),
    );
    if (!currentPlan || currentPlan.maxDashboards === null) return null;
    return `${this.formatLimit(currentPlan.maxDashboards)} Dashboards`;
  }

  private handleOAuthRedirectResult(): void {
    const oauth = this.route.snapshot.queryParamMap.get("oauth");
    const message = this.route.snapshot.queryParamMap.get("message");
    if (!oauth) {
      return;
    }

    if (oauth === "success") {
      this.showSuccess("Jira OAuth connected successfully.");
      this.error.set(null);
    } else if (oauth === "error") {
      this.error.set(message || "Jira OAuth connection failed. Please retry.");
      this.success.set(null);
    }

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { oauth: null, message: null },
      queryParamsHandling: "merge",
      replaceUrl: true,
    });
  }

  private loadConfig(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api
      .getConfig()
      .pipe(timeout(10000))
      .subscribe({
        next: (cfg) => {
          if (cfg) {
            this.config.set(cfg);
            this.form = {
              baseUrl: cfg.baseUrl,
              authType: (cfg.authType as JiraAuthType) || "BASIC",
              userEmail: cfg.userEmail || "",
              token: cfg.token,
            };
          }
          // 204 No Content → cfg is null, show empty form
          this.loading.set(false);
        },
        error: (err) => {
          if (err?.name === "TimeoutError") {
            this.error.set(
              "Server did not respond in time. Is the backend running?",
            );
          } else {
            this.error.set(err.message ?? "Failed to load configuration");
          }
          this.loading.set(false);
        },
      });
  }

  private loadDashboards(): void {
    this.dashboardsLoading.set(true);
    this.api.listDashboards().subscribe({
      next: (dashboards) => {
        const orderedDashboards = [...dashboards].sort(
          (a, b) => a.position - b.position,
        );
        this.dashboards.set(orderedDashboards);
        this.dashboardsLoading.set(false);
      },
      error: (err) => {
        this.error.set(err.message ?? "Failed to load dashboards.");
        this.dashboardsLoading.set(false);
      },
    });
  }

  private validateWizardInput(): boolean {
    if (!this.form.baseUrl.trim() || !this.form.token.trim()) {
      this.error.set("Base URL and token are required.");
      return false;
    }

    if (this.form.authType === "BASIC" && !this.form.userEmail.trim()) {
      this.error.set("User email is required with BASIC auth.");
      return false;
    }

    return true;
  }

  private validateServerConfigInput(): boolean {
    if (!this.form.baseUrl.trim()) {
      this.error.set("Jira Base URL is required.");
      return false;
    }

    if (!this.form.userEmail.trim()) {
      this.error.set("User email/login is required.");
      return false;
    }

    if (!this.form.token.trim()) {
      this.error.set("AD/SSO password is required.");
      return false;
    }

    return true;
  }

  useAmadeusDefaults(): void {
    this.form.baseUrl = "https://jira.amadeus.com/agile";
    this.form.authType = "BASIC";
  }

  onSaveAndTestServerConfig(): void {
    if (!this.isAdmin) {
      return;
    }

    if (!this.validateServerConfigInput()) {
      return;
    }

    this.configuringServer.set(true);
    this.error.set(null);
    this.success.set(null);

    this.api
      .updateCredentials({
        baseUrl: this.form.baseUrl.trim() || "https://jira.amadeus.com/agile",
        authType: "BASIC",
        userEmail: this.form.userEmail.trim(),
        token: this.form.token,
      })
      .subscribe({
        next: () => {
          this.api.testConnection().subscribe({
            next: (res) => {
              if (!res.success) {
                this.error.set(
                  res.message ||
                    "Jira test failed. Please check credentials and board settings.",
                );
                this.configuringServer.set(false);
                return;
              }

              this.showSuccess(
                "Jira server-to-server configuration saved and validated.",
              );
              this.configuringServer.set(false);
              this.editingCredentials.set(false);
              this.credentialsVerified.set(true);
              this.loadConfig();
              this.loadDashboards();
            },
            error: (err) => {
              this.error.set(err.message ?? "Jira test failed.");
              this.configuringServer.set(false);
            },
          });
        },
        error: (err) => {
          this.error.set(err.message ?? "Unable to save Jira configuration.");
          this.configuringServer.set(false);
        },
      });
  }

  onDiscoverBoards(): void {
    if (!this.validateWizardInput()) {
      return;
    }

    this.discoveringBoards.set(true);
    this.error.set(null);
    this.success.set(null);
    this.discoveredBoards.set([]);
    this.selectedBoardId.set(null);

    this.api
      .discoverBoards({
        baseUrl: this.form.baseUrl.trim(),
        authType: this.form.authType,
        userEmail:
          this.form.authType === "BASIC"
            ? this.form.userEmail.trim()
            : undefined,
        token: this.form.token.trim(),
      })
      .subscribe({
        next: (boards) => {
          this.discoveredBoards.set(boards);
          if (boards.length > 0) {
            this.selectedBoardId.set(boards[0].id);
            this.showSuccess(
              "Boards discovered. Select one and create dashboard.",
            );
          } else {
            this.error.set("No boards found for this Jira account.");
          }
          this.discoveringBoards.set(false);
        },
        error: (err) => {
          this.error.set(err.message ?? "Unable to discover Jira boards.");
          this.discoveringBoards.set(false);
        },
      });
  }

  onAddDashboard(): void {
    const { name, projectKey, boardId } = this.newDashboard;
    if (!name.trim() || !projectKey.trim() || !boardId || boardId <= 0) {
      this.error.set("Nom, Project Key et Board ID sont requis.");
      return;
    }

    this.creatingDashboard.set(true);
    this.error.set(null);
    this.success.set(null);
    this.api
      .createDashboard({
        name: name.trim(),
        boardId: Number(boardId),
        projectKey: projectKey.trim().toUpperCase(),
      })
      .subscribe({
        next: () => {
          this.showSuccess(`Dashboard "${name.trim()}" créé.`);
          this.creatingDashboard.set(false);
          this.newDashboard = { name: "", projectKey: "", boardId: null };
          this.loadDashboards();
        },
        error: (err) => {
          this.error.set(
            err.status === 402
              ? `Dashboard limit reached (${this.dashboardLimitLabel ?? "Plan limit"}). Upgrade your plan to add more dashboards.`
              : (err.message ?? "Impossible de créer le dashboard."),
          );
          this.creatingDashboard.set(false);
        },
      });
  }

  onCreateDashboard(): void {
    const boardId = this.selectedBoardId();
    const board = this.discoveredBoards().find((b) => b.id === boardId);
    if (!board) {
      this.error.set("Please select a board first.");
      return;
    }

    this.creatingDashboard.set(true);
    this.error.set(null);
    this.success.set(null);
    this.api
      .createDashboard({
        name: board.name,
        boardId: board.id,
        projectKey: board.projectKey,
      })
      .subscribe({
        next: () => {
          this.showSuccess(`Dashboard '${board.name}' created.`);
          this.creatingDashboard.set(false);
          this.loadDashboards();
          this.loadConfig();
        },
        error: (err) => {
          this.error.set(
            err.status === 402
              ? `Dashboard limit reached (${this.dashboardLimitLabel ?? "Plan limit"}). Upgrade your plan to add more dashboards.`
              : (err.message ?? "Unable to create dashboard."),
          );
          this.creatingDashboard.set(false);
        },
      });
  }

  onActivateDashboard(dashboardId: string): void {
    this.switchingDashboardId.set(dashboardId);
    this.error.set(null);
    this.success.set(null);
    this.api.activateDashboard(dashboardId).subscribe({
      next: () => {
        this.showSuccess("Active dashboard updated.");
        this.switchingDashboardId.set(null);
        this.loadDashboards();
      },
      error: (err) => {
        this.error.set(err.message ?? "Unable to switch dashboard.");
        this.switchingDashboardId.set(null);
      },
    });
  }

  onDeleteDashboard(dashboard: JiraDashboard): void {
    const confirmed = globalThis.confirm(
      `Delete dashboard '${dashboard.name}'?`,
    );
    if (!confirmed) {
      return;
    }

    this.deletingDashboardId.set(dashboard.id);
    this.error.set(null);
    this.success.set(null);
    this.api.deleteDashboard(dashboard.id).subscribe({
      next: () => {
        this.showSuccess("Dashboard deleted.");
        this.deletingDashboardId.set(null);
        this.loadDashboards();
      },
      error: (err) => {
        this.error.set(err.message ?? "Unable to delete dashboard.");
        this.deletingDashboardId.set(null);
      },
    });
  }

  onAuthTypeChange(): void {
    if (this.form.authType === "PAT") {
      this.form.userEmail = "";
    }
  }

  onSync(): void {
    this.syncing.set(true);
    this.error.set(null);
    this.success.set(null);
    this.api.sync().subscribe({
      next: () => {
        this.showSuccess("Sync completed.");
        this.syncing.set(false);
      },
      error: (err) => {
        this.error.set(err.message);
        this.syncing.set(false);
      },
    });
  }

  get activeDashboard(): JiraDashboard | null {
    return this.dashboards().find((dashboard) => dashboard.active) ?? null;
  }

  get canCreateDashboard(): boolean {
    return (
      this.isAdmin &&
      this.selectedBoardId() !== null &&
      !this.creatingDashboard()
    );
  }

  get isAdmin(): boolean {
    return this.authState.user()?.role === "ADMIN";
  }

  get isConnected(): boolean {
    // true if OAuth connected OR BASIC credentials were verified (server test passed)
    // OR if config already has credentials saved (baseUrl + userEmail set)
    const cfg = this.config();
    return (
      this.credentialsVerified() ||
      cfg?.connected === true ||
      (!!cfg?.baseUrl && !!cfg?.userEmail)
    );
  }

  startEditCredentials(): void {
    const cfg = this.config();
    if (cfg) {
      this.form = {
        baseUrl: cfg.baseUrl,
        authType: (cfg.authType as JiraAuthType) || "BASIC",
        userEmail: cfg.userEmail || "",
        token: "",
      };
    }
    this.editingCredentials.set(true);
    this.error.set(null);
  }

  onConnectOAuth(): void {
    this.connecting.set(true);
    this.error.set(null);
    this.success.set(null);
    const oauthWindow = globalThis.open("about:blank", "_blank");
    if (!oauthWindow) {
      this.error.set("Pop-up blocked. Please allow pop-ups and retry.");
      this.connecting.set(false);
      return;
    }

    this.api.startOAuthConnect().subscribe({
      next: ({ authUrl }) => {
        if (!authUrl) {
          this.error.set("OAuth URL was not provided by the server.");
          oauthWindow.close();
          this.connecting.set(false);
          return;
        }
        oauthWindow.location.href = authUrl;
        this.connecting.set(false);
      },
      error: (err) => {
        this.error.set(err.message ?? "Unable to initiate Jira OAuth.");
        oauthWindow.close();
        this.connecting.set(false);
      },
    });
  }

  onDisconnectOAuth(): void {
    this.disconnecting.set(true);
    this.error.set(null);
    this.success.set(null);
    this.api.disconnectOAuth().subscribe({
      next: () => {
        this.showSuccess("Jira OAuth disconnected.");
        this.disconnecting.set(false);
        this.loadConfig();
        this.loadDashboards();
      },
      error: (err) => {
        this.error.set(err.message ?? "Unable to disconnect Jira OAuth.");
        this.disconnecting.set(false);
      },
    });
  }
}
