import { Injectable, inject, signal, computed } from "@angular/core";
import { Router } from "@angular/router";
import { AuthApiService } from "./auth-api.service";
import { PermissionService } from "./permission.service";
import { OrganizationStateService } from "./organization-state.service";
import { SubscriptionStateService } from "./subscription-state.service";
import { SprintStateService } from "../../features/sprint/services/sprint-state.service";
import { CapacityStateService } from "../../features/sprint/services/capacity-state.service";
import { CurrentIterationService } from "../../features/sprint/services/current-iteration.service";
import { JiraCredentialsStateService } from "./jira-credentials-state.service";
import {
  User,
  LoginRequest,
  RegisterRequest,
  AuthResponse,
} from "../models/user.model";

const TOKEN_KEY = "sr_token";
const REFRESH_TOKEN_KEY = "sr_refresh_token";
const TOKEN_EXPIRY_KEY = "sr_token_expiry";

@Injectable({ providedIn: "root" })
export class AuthStateService {
  private readonly api = inject(AuthApiService);
  private readonly router = inject(Router);
  private readonly permissionService = inject(PermissionService);
  private readonly orgState = inject(OrganizationStateService);
  private readonly subState = inject(SubscriptionStateService);
  private readonly sprintState = inject(SprintStateService);
  private readonly capacityState = inject(CapacityStateService);
  private readonly currentIteration = inject(CurrentIterationService);
  private readonly jiraCredentialsState = inject(JiraCredentialsStateService);

  private readonly _user = signal<User | null>(null);
  private readonly _loading = signal(false);
  private readonly _error = signal<string | null>(null);
  private readonly _registerSuccess = signal<string | null>(null);
  /** Non-null when the last login failed because the email is not verified. Holds the attempted email. */
  private readonly _emailUnverified = signal<string | null>(null);
  private readonly _resendLoading = signal(false);
  private readonly _resendSuccess = signal(false);

  readonly user = this._user.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();
  readonly registerSuccess = this._registerSuccess.asReadonly();
  readonly emailUnverified = this._emailUnverified.asReadonly();
  readonly resendLoading = this._resendLoading.asReadonly();
  readonly resendSuccess = this._resendSuccess.asReadonly();
  readonly isAuthenticated = computed(
    () => !!this._user() && !!this.getToken(),
  );

  getToken(): string | null {
    const token = localStorage.getItem(TOKEN_KEY);
    const expiry = localStorage.getItem(TOKEN_EXPIRY_KEY);
    if (!token || !expiry) return null;
    if (Date.now() > Number(expiry)) {
      this.clearToken();
      return null;
    }
    return token;
  }

  login(request: LoginRequest): void {
    this._loading.set(true);
    this._error.set(null);
    this._emailUnverified.set(null);
    const shouldRedirectToHome = this.router.url.startsWith("/auth");
    this.api.login(request).subscribe({
      next: (res) => this.handleAuthSuccess(res, shouldRedirectToHome),
      error: (err) => {
        if (err.emailUnverified) {
          this._emailUnverified.set(request.email);
          this._error.set(null);
        } else {
          this._error.set(err.message ?? "Login failed");
        }
        this._loading.set(false);
      },
    });
  }

  resendVerificationEmail(email: string): void {
    this._resendLoading.set(true);
    this._resendSuccess.set(false);
    this.api.resendVerificationEmail(email).subscribe({
      next: () => {
        this._resendLoading.set(false);
        this._resendSuccess.set(true);
      },
      error: () => {
        this._resendLoading.set(false);
      },
    });
  }

  register(request: RegisterRequest): void {
    this._loading.set(true);
    this._error.set(null);
    this._registerSuccess.set(null);
    this.api.register(request).subscribe({
      next: (res) => {
        this._loading.set(false);
        this._registerSuccess.set(res.message);
      },
      error: (err) => {
        this._error.set(err.message ?? "Registration failed");
        this._loading.set(false);
      },
    });
  }

  loadCurrentUser(): void {
    if (!this.getToken()) return;
    this._loading.set(true);
    this.api.me().subscribe({
      next: (user) => {
        this._user.set(user);
        this._loading.set(false);
        this.permissionService.loadPermissions();
        if (user.role === "ADMIN") {
          this.subState.loadSubscription();
          this.subState.loadFeatures();
        }
      },
      error: () => {
        this.clearToken();
        this._loading.set(false);
      },
    });
  }

  logout(): void {
    this.api.logout().subscribe({ error: () => {} });
    this.clearToken();
    this._user.set(null);
    this._error.set(null);
    this._registerSuccess.set(null);
    this._emailUnverified.set(null);
    this._resendSuccess.set(false);
    this.permissionService.clear();
    this.orgState.clear();
    this.subState.clear();
    this.sprintState.clear();
    this.capacityState.clear();
    this.currentIteration.clear();
    this.jiraCredentialsState.clear();
    this.router.navigate(["/auth/login"]);
  }

  clearError(): void {
    this._error.set(null);
    this._registerSuccess.set(null);
    this._emailUnverified.set(null);
    this._resendSuccess.set(false);
  }

  private handleAuthSuccess(
    res: AuthResponse,
    shouldRedirectToHome = true,
  ): void {
    this.storeToken(res.accessToken, res.refreshToken, res.expiresIn);
    this._user.set(res.user);
    this._loading.set(false);
    this.permissionService.loadPermissions();
    this.orgState.loadOrganization();
    if (res.user.role === "ADMIN") {
      this.subState.loadSubscription();
      this.subState.loadFeatures();
    }
    if (shouldRedirectToHome && this.router.url.startsWith("/auth")) {
      if (res.user.role !== "ADMIN") {
        // Non-admins: check personal Jira connection and redirect accordingly
        this.jiraCredentialsState.load().subscribe({
          next: (creds) => {
            this.router.navigate(creds.connected ? ["/"] : ["/settings/jira"]);
          },
          error: () => {
            this.router.navigate(["/"]);
          },
        });
      } else {
        this.router.navigate(["/"]);
      }
    }
  }

  private storeToken(
    accessToken: string,
    refreshToken: string,
    expiresIn: number,
  ): void {
    localStorage.setItem(TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
    localStorage.setItem(
      TOKEN_EXPIRY_KEY,
      String(Date.now() + expiresIn * 1000),
    );
  }

  private clearToken(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(TOKEN_EXPIRY_KEY);
  }
}
