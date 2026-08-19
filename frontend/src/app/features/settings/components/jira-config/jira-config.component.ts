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
  MyJiraCredentials,
} from "../../../../core/models/jira-config.model";
import { JiraCredentialsStateService } from "../../../../core/services/jira-credentials-state.service";
import { I18nService } from "../../../../i18n/i18n.service";

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
  private readonly jiraCreds = inject(JiraCredentialsStateService);
  private readonly i18n = inject(I18nService);

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
  readonly myCredentials = signal<MyJiraCredentials | null>(null);
  readonly savingPersonal = signal(false);
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

  personalForm = { baseUrl: "", username: "", password: "" };

  ngOnInit(): void {
    this.handleOAuthRedirectResult();
    if (this.isAdmin) {
      this.loadConfig();
      this.loadDashboards();
      if (!this.subState.subscription()) this.subState.loadSubscription();
      if (!this.subState.plans().length) this.subState.loadPlans();
    } else {
      this.loadMyCredentials();
    }
  }

  formatLimit(value: number | null): string {
    return value === null ? this.i18n.t("common.unlimited") : String(value);
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
      this.showSuccess("jira.success.oauth_connected");
      this.error.set(null);
    } else if (oauth === "error") {
      this.error.set(message || "jira.oauth.error");
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
            this.error.set("jira.error.timeout");
          } else {
            this.error.set(err.message ?? "jira.error.load_config");
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
        this.error.set(err.message ?? "jira.error.load_dashboards");
        this.dashboardsLoading.set(false);
      },
    });
  }

  private validateWizardInput(): boolean {
    if (!this.form.baseUrl.trim() || !this.form.token.trim()) {
      this.error.set("jira.error.base_url_token_required");
      return false;
    }

    if (this.form.authType === "BASIC" && !this.form.userEmail.trim()) {
      this.error.set("jira.error.email_required_basic");
      return false;
    }

    return true;
  }

  private validateServerConfigInput(): boolean {
    if (!this.form.baseUrl.trim()) {
      this.error.set("jira.error.base_url_required");
      return false;
    }

    if (this.form.authType === "BASIC" && !this.form.userEmail.trim()) {
      this.error.set("jira.error.email_required");
      return false;
    }

    if (!this.form.token.trim()) {
      this.error.set("jira.error.password_required");
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
        authType: this.form.authType,
        userEmail:
          this.form.authType === "BASIC" ? this.form.userEmail.trim() : "",
        token: this.form.token,
      })
      .subscribe({
        next: () => {
          this.api.testConnection().subscribe({
            next: (res) => {
              if (!res.success) {
                this.error.set(res.message ?? "jira.error.test_failed");
                this.configuringServer.set(false);
                return;
              }

              this.showSuccess("jira.success.config_saved");
              this.configuringServer.set(false);
              this.editingCredentials.set(false);
              this.credentialsVerified.set(true);
              this.loadConfig();
              this.loadDashboards();
            },
            error: (err) => {
              this.error.set(err.message ?? "jira.error.test_failed");
              this.configuringServer.set(false);
            },
          });
        },
        error: (err) => {
          this.error.set(err.message ?? "jira.error.save_config");
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
            this.showSuccess("jira.success.boards_discovered");
          } else {
            this.error.set("jira.error.no_boards");
          }
          this.discoveringBoards.set(false);
        },
        error: (err) => {
          this.error.set(err.message ?? "jira.error.discover_boards");
          this.discoveringBoards.set(false);
        },
      });
  }

  onAddDashboard(): void {
    const { name, projectKey, boardId } = this.newDashboard;
    if (!name.trim() || !projectKey.trim() || !boardId || boardId <= 0) {
      this.error.set("jira.error.dashboard_fields_required");
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
          this.showSuccess(
            this.i18n.tWithParams("jira.success.dashboard_created", {
              name: name.trim(),
            }),
          );
          this.creatingDashboard.set(false);
          this.newDashboard = { name: "", projectKey: "", boardId: null };
          this.loadDashboards();
        },
        error: (err) => {
          this.error.set(
            err.status === 402
              ? "jira.error.dashboard_limit"
              : (err.message ?? "jira.error.dashboard_create"),
          );
          this.creatingDashboard.set(false);
        },
      });
  }

  onCreateDashboard(): void {
    const boardId = this.selectedBoardId();
    const board = this.discoveredBoards().find((b) => b.id === boardId);
    if (!board) {
      this.error.set("jira.error.select_board");
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
          this.showSuccess(
            this.i18n.tWithParams("jira.success.dashboard_created", {
              name: board.name,
            }),
          );
          this.creatingDashboard.set(false);
          this.loadDashboards();
          this.loadConfig();
        },
        error: (err) => {
          this.error.set(
            err.status === 402
              ? "jira.error.dashboard_limit"
              : (err.message ?? "jira.error.dashboard_create"),
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
        this.showSuccess("jira.success.dashboard_activated");
        this.switchingDashboardId.set(null);
        this.loadDashboards();
      },
      error: (err) => {
        this.error.set(err.message ?? "jira.error.dashboard_switch");
        this.switchingDashboardId.set(null);
      },
    });
  }

  onDeleteDashboard(dashboard: JiraDashboard): void {
    const confirmed = globalThis.confirm(
      this.i18n.tWithParams("jira.dashboards.confirm.delete", {
        name: dashboard.name,
      }),
    );
    if (!confirmed) {
      return;
    }

    this.deletingDashboardId.set(dashboard.id);
    this.error.set(null);
    this.success.set(null);
    this.api.deleteDashboard(dashboard.id).subscribe({
      next: () => {
        this.showSuccess("jira.success.dashboard_deleted");
        this.deletingDashboardId.set(null);
        this.loadDashboards();
      },
      error: (err) => {
        this.error.set(err.message ?? "jira.error.dashboard_delete");
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
        this.showSuccess("jira.success.sync");
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
    // OR if personal credentials are saved (for non-admins)
    const cfg = this.config();
    const personalCreds = this.myCredentials();
    return (
      this.credentialsVerified() ||
      cfg?.connected === true ||
      (!!cfg?.baseUrl && !!cfg?.userEmail) ||
      !!personalCreds?.username
    );
  }

  private loadMyCredentials(): void {
    const cached = this.jiraCreds.credentials();
    if (cached !== null) {
      this.myCredentials.set(cached);
      if (cached.username) this.loadDashboards();
      return;
    }
    this.jiraCreds.load().subscribe({
      next: (creds) => {
        this.myCredentials.set(creds);
        if (creds?.username) this.loadDashboards();
      },
      error: () => {},
    });
  }

  onSavePersonalCredentials(): void {
    if (
      !this.personalForm.baseUrl.trim() ||
      !this.personalForm.username.trim() ||
      !this.personalForm.password.trim()
    ) {
      this.error.set("jira.error.username_password_required");
      return;
    }

    this.savingPersonal.set(true);
    this.error.set(null);

    this.api
      .saveMyCredentials({
        baseUrl: this.personalForm.baseUrl.trim(),
        username: this.personalForm.username.trim(),
        password: this.personalForm.password,
      })
      .subscribe({
        next: (creds) => {
          this.jiraCreds.setConnected(creds.username!, creds.baseUrl!);
          this.myCredentials.set(creds);
          this.personalForm.password = "";
          this.editingCredentials.set(false);
          this.savingPersonal.set(false);
          this.loadDashboards();
          this.showSuccess("jira.success.personal_connected");
        },
        error: (err) => {
          this.error.set(err.message ?? "jira.error.save_credentials");
          this.savingPersonal.set(false);
        },
      });
  }

  startEditCredentials(): void {
    if (this.isAdmin) {
      const cfg = this.config();
      if (cfg) {
        this.form = {
          baseUrl: cfg.baseUrl,
          authType: (cfg.authType as JiraAuthType) || "BASIC",
          userEmail: cfg.userEmail || "",
          token: "",
        };
      }
    } else {
      const creds = this.myCredentials();
      if (creds) {
        this.personalForm.baseUrl = creds.baseUrl || "";
        this.personalForm.username = creds.username || "";
        this.personalForm.password = "";
      }
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
      this.error.set("jira.error.popup_blocked");
      this.connecting.set(false);
      return;
    }

    this.api.startOAuthConnect().subscribe({
      next: ({ authUrl }) => {
        if (!authUrl) {
          this.error.set("jira.error.oauth_url_missing");
          oauthWindow.close();
          this.connecting.set(false);
          return;
        }
        oauthWindow.location.href = authUrl;
        this.connecting.set(false);
      },
      error: (err) => {
        this.error.set(err.message ?? "jira.error.oauth_init");
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
        this.showSuccess("jira.success.oauth_disconnected");
        this.disconnecting.set(false);
        this.loadConfig();
        this.loadDashboards();
      },
      error: (err) => {
        this.error.set(err.message ?? "jira.error.oauth_disconnect");
        this.disconnecting.set(false);
      },
    });
  }
}
