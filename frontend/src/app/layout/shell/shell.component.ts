import { Component, inject, ViewChild } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterOutlet } from "@angular/router";
import { SidebarComponent } from "../sidebar/sidebar.component";
import { AuthStateService } from "../../core/services/auth-state.service";
import { OrganizationStateService } from "../../core/services/organization-state.service";
import { InvitationApiService } from "../../core/services/invitation-api.service";

@Component({
  selector: "app-shell",
  standalone: true,
  imports: [CommonModule, RouterOutlet, SidebarComponent],
  templateUrl: "./shell.component.html",
  styleUrls: ["./shell.component.scss"],
})
export class ShellComponent {
  readonly authState = inject(AuthStateService);
  readonly orgState = inject(OrganizationStateService);
  private readonly invitationApi = inject(InvitationApiService);
  @ViewChild("sidebar") sidebar!: SidebarComponent;

  resendingEmail = false;
  resendSuccess = false;

  get showEmailBanner(): boolean {
    const user = this.authState.user();
    return !!user && user.emailVerified === false;
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
