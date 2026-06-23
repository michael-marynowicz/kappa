import { Component, inject, OnInit, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { InvitationApiService } from "../../../../core/services/invitation-api.service";
import { AuthStateService } from "../../../../core/services/auth-state.service";
import { AuthApiService } from "../../../../core/services/auth-api.service";
import { VerifyEmailResponse } from "../../../../core/models/invitation.model";
import { HttpErrorResponse } from "@angular/common/http";

type VerifyState = "loading" | "success" | "joined" | "invalid" | "expired";

@Component({
  selector: "app-verify-email",
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: "./verify-email.component.html",
  styleUrls: ["./verify-email.component.scss"],
})
export class VerifyEmailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly invitationApi = inject(InvitationApiService);
  private readonly authState = inject(AuthStateService);
  private readonly authApi = inject(AuthApiService);

  readonly state = signal<VerifyState>("loading");
  readonly result = signal<VerifyEmailResponse | null>(null);
  readonly resending = signal(false);
  readonly resent = signal(false);

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get("token");
    if (!token) {
      this.state.set("invalid");
      return;
    }

    this.invitationApi.verifyEmail(token).subscribe({
      next: (response) => {
        this.result.set(response);
        if (response.joinedOrganization) {
          this.state.set("joined");
          this.refreshToken();
        } else {
          this.state.set("success");
        }
      },
      error: (err: HttpErrorResponse) => {
        if (err.status === 410) {
          this.state.set("expired");
        } else {
          this.state.set("invalid");
        }
      },
    });
  }

  resendVerification(): void {
    this.resending.set(true);
    this.invitationApi.resendVerification().subscribe({
      next: () => {
        this.resending.set(false);
        this.resent.set(true);
      },
      error: () => {
        this.resending.set(false);
      },
    });
  }

  goToDashboard(): void {
    const dashboardId = this.result()?.dashboardId;
    if (dashboardId) {
      this.router.navigate(["/sprint"], {
        queryParams: { dashboard: dashboardId },
      });
    } else {
      this.router.navigate(["/"]);
    }
  }

  private refreshToken(): void {
    const refreshToken = localStorage.getItem("sr_refresh_token");
    if (refreshToken) {
      this.authApi.refresh({ refreshToken }).subscribe({
        next: (res) => {
          localStorage.setItem("sr_token", res.accessToken);
          localStorage.setItem("sr_refresh_token", res.refreshToken);
          localStorage.setItem(
            "sr_token_expiry",
            String(Date.now() + res.expiresIn * 1000),
          );
          this.authState.loadCurrentUser();
        },
        error: () => {},
      });
    }
  }
}
