import { Component, inject, OnInit, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { timeout } from "rxjs/operators";
import { JiraConfigApiService } from "../../../../core/services/jira-config-api.service";
import { JiraConfig } from "../../../../core/models/jira-config.model";

@Component({
  selector: "app-jira-config",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./jira-config.component.html",
  styleUrls: ["./jira-config.component.scss"],
})
export class JiraConfigComponent implements OnInit {
  private readonly api = inject(JiraConfigApiService);

  readonly config = signal<JiraConfig | null>(null);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly testing = signal(false);
  readonly syncing = signal(false);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);

  form = {
    baseUrl: "",
    authType: "PAT",
    userEmail: "",
    token: "",
    projectKey: "",
    boardId: null as unknown as number,
  };

  ngOnInit(): void {
    this.loadConfig();
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
              authType: cfg.authType || "PAT",
              userEmail: cfg.userEmail || "",
              token: cfg.token,
              projectKey: cfg.projectKey,
              boardId: cfg.boardId,
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

  onSave(): void {
    this.saving.set(true);
    this.error.set(null);
    this.success.set(null);
    const payload = {
      baseUrl: this.form.baseUrl,
      authType: this.form.authType,
      userEmail: this.form.userEmail,
      token: this.form.token,
      projectKey: this.form.projectKey,
      boardId: this.form.boardId,
    };
    this.api.updateConfig(payload).subscribe({
      next: (cfg) => {
        this.config.set(cfg);
        this.success.set("Configuration saved successfully.");
        this.saving.set(false);
      },
      error: (err) => {
        this.error.set(err.message);
        this.saving.set(false);
      },
    });
  }

  onTestConnection(): void {
    this.testing.set(true);
    this.error.set(null);
    this.success.set(null);
    this.api.testConnection().subscribe({
      next: (res) => {
        if (res.success) {
          this.success.set("Connection successful!");
        } else {
          this.error.set(res.message);
        }
        this.testing.set(false);
      },
      error: (err) => {
        this.error.set(err.message);
        this.testing.set(false);
      },
    });
  }

  onSync(): void {
    this.syncing.set(true);
    this.api.sync().subscribe({
      next: () => {
        this.success.set("Sync completed.");
        this.syncing.set(false);
      },
      error: (err) => {
        this.error.set(err.message);
        this.syncing.set(false);
      },
    });
  }
}
