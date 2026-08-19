import { Component, inject, signal, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { Router } from "@angular/router";
import { TranslatePipe } from "../../../../shared/pipes/translate.pipe";
import { JiraConfigApiService } from "../../../../core/services/jira-config-api.service";
import { JiraCredentialsStateService } from "../../../../core/services/jira-credentials-state.service";

@Component({
  selector: "app-my-jira-credentials",
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: "./my-jira-credentials.component.html",
  styleUrls: ["./my-jira-credentials.component.scss"],
})
export class MyJiraCredentialsComponent implements OnInit {
  private readonly api = inject(JiraConfigApiService);
  private readonly jiraCreds = inject(JiraCredentialsStateService);
  private readonly router = inject(Router);

  readonly connected = signal(false);
  readonly connectedUsername = signal<string | undefined>(undefined);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);
  readonly editing = signal(false);

  form = {
    baseUrl: "https://jira.amadeus.com/agile",
    username: "",
    password: "",
  };

  ngOnInit(): void {
    // Use cached state if available, otherwise load
    const cached = this.jiraCreds.credentials();
    if (cached !== null) {
      this.applyCredentials(cached);
    } else {
      this.jiraCreds.load().subscribe({
        next: (creds) => this.applyCredentials(creds),
        error: () => {},
      });
    }
  }

  private applyCredentials(creds: {
    connected: boolean;
    username?: string;
  }): void {
    this.connected.set(creds.connected);
    this.connectedUsername.set(creds.username);
    if (!creds.connected) {
      this.editing.set(true);
    }
  }

  startEditing(): void {
    const creds = this.jiraCreds.credentials();
    this.form = {
      baseUrl: creds?.baseUrl ?? "https://jira.amadeus.com/agile",
      username: this.connectedUsername() ?? "",
      password: "",
    };
    this.error.set(null);
    this.success.set(null);
    this.editing.set(true);
  }

  cancelEditing(): void {
    this.editing.set(false);
    this.error.set(null);
  }

  submit(): void {
    if (!this.form.username.trim() || !this.form.password.trim()) {
      this.error.set("my_jira.error.required");
      return;
    }

    this.saving.set(true);
    this.error.set(null);

    this.api
      .saveMyCredentials({
        baseUrl: this.form.baseUrl.trim(),
        username: this.form.username.trim(),
        password: this.form.password,
      })
      .subscribe({
        next: (creds) => {
          this.saving.set(false);
          this.jiraCreds.setConnected(creds.username!);
          this.connected.set(true);
          this.connectedUsername.set(creds.username);
          this.editing.set(false);
          this.success.set("my_jira.success");
          this.router.navigate(["/"]);
        },
        error: (err) => {
          this.saving.set(false);
          this.error.set(err?.message ?? "my_jira.error.save_failed");
        },
      });
  }
}
