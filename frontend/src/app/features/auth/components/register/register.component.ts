import { Component, inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { RouterLink } from "@angular/router";
import { AuthStateService } from "../../../../core/services/auth-state.service";
import { InvitationApiService } from "../../../../core/services/invitation-api.service";
import { InvitationCheck } from "../../../../core/models/invitation.model";

@Component({
  selector: "app-register",
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: "./register.component.html",
  styleUrls: ["./register.component.scss"],
})
export class RegisterComponent {
  readonly authState = inject(AuthStateService);
  private readonly invitationApi = inject(InvitationApiService);

  email = "";
  password = "";
  firstName = "";
  lastName = "";
  organizationName = "";
  showPassword = false;

  invitationCheck: InvitationCheck | null = null;
  checkingInvitation = false;

  onEmailBlur(): void {
    const email = this.email.trim();
    if (!email?.includes("@")) {
      this.invitationCheck = null;
      return;
    }

    this.checkingInvitation = true;
    this.invitationApi.checkInvitation(email).subscribe({
      next: (result) => {
        this.invitationCheck = result;
        this.checkingInvitation = false;
      },
      error: () => {
        this.invitationCheck = null;
        this.checkingInvitation = false;
      },
    });
  }

  get isInvited(): boolean {
    return this.invitationCheck?.invited === true;
  }

  onSubmit(): void {
    this.authState.register({
      email: this.email,
      password: this.password,
      firstName: this.firstName,
      lastName: this.lastName,
      organizationName: this.isInvited
        ? (this.invitationCheck!.organizationName ?? "Invited")
        : this.organizationName,
    });
  }
}
