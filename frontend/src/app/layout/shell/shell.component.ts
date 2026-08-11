import { Component, inject, OnInit, ViewChild } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterLink, RouterOutlet } from "@angular/router";
import { SidebarComponent } from "../sidebar/sidebar.component";
import { AuthStateService } from "../../core/services/auth-state.service";
import { OrganizationStateService } from "../../core/services/organization-state.service";
import { InvitationApiService } from "../../core/services/invitation-api.service";
import { JiraCredentialsStateService } from "../../core/services/jira-credentials-state.service";
import { LanguageSettingsApiService } from "../../core/services/language-settings-api.service";
import { I18nService } from "../../i18n/i18n.service";
import { TranslatePipe } from "../../shared/pipes/translate.pipe";

@Component({
  selector: "app-shell",
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    SidebarComponent,
    TranslatePipe,
  ],
  templateUrl: "./shell.component.html",
  styleUrls: ["./shell.component.scss"],
})
export class ShellComponent implements OnInit {
  readonly authState = inject(AuthStateService);
  readonly orgState = inject(OrganizationStateService);
  private readonly invitationApi = inject(InvitationApiService);
  private readonly jiraCreds = inject(JiraCredentialsStateService);
  private readonly languageApi = inject(LanguageSettingsApiService);
  private readonly i18n = inject(I18nService);
  @ViewChild("sidebar") sidebar!: SidebarComponent;

  resendingEmail = false;
  resendSuccess = false;

  ngOnInit(): void {
    // Sync language from backend on every shell load so cross-device preference is respected
    this.languageApi.getLanguageSettings().subscribe({
      next: (s) => {
        if (s.language !== this.i18n.language())
          this.i18n.setLanguage(s.language);
      },
      error: () => {},
    });
    const user = this.authState.user();
    // Load Jira credentials for non-admins so the banner is reactive on first visit
    if (
      user &&
      user.role !== "ADMIN" &&
      this.jiraCreds.credentials() === null
    ) {
      this.jiraCreds.load().subscribe({ error: () => {} });
    }
  }

  get showEmailBanner(): boolean {
    const user = this.authState.user();
    return !!user && user.emailVerified === false;
  }

  get showJiraBanner(): boolean {
    const user = this.authState.user();
    if (!user || user.role === "ADMIN") return false;
    const creds = this.jiraCreds.credentials();
    return creds !== null && !creds.connected;
  }

  resendVerification(): void {
    this.resendingEmail = true;
    this.invitationApi.resendVerification().subscribe({
      next: () => {
        this.resendingEmail = false;
        this.resendSuccess = true;
        setTimeout(() => (this.resendSuccess = false), 5000);
      },
      error: () => {
        this.resendingEmail = false;
      },
    });
  }
}
