import { Injectable, inject, signal, computed } from "@angular/core";
import { Router } from "@angular/router";
import { AuthApiService } from "./auth-api.service";
import { PermissionService } from "./permission.service";
import { OrganizationStateService } from "./organization-state.service";
import { SubscriptionStateService } from "./subscription-state.service";
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

  private readonly _user = signal<User | null>(null);
  private readonly _loading = signal(false);
  private readonly _error = signal<string | null>(null);
  private readonly _registerSuccess = signal<string | null>(null);

  readonly user = this._user.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();
  readonly registerSuccess = this._registerSuccess.asReadonly();
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
    this.api.login(request).subscribe({
      next: (res) => this.handleAuthSuccess(res),
      error: (err) => {
        this._error.set(err.message ?? "Login failed");
        this._loading.set(false);
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
    this.permissionService.clear();
    this.router.navigate(["/auth/login"]);
  }

  clearError(): void {
    this._error.set(null);
    this._registerSuccess.set(null);
  }

  private handleAuthSuccess(res: AuthResponse): void {
    this.storeToken(res.accessToken, res.refreshToken, res.expiresIn);
    this._user.set(res.user);
    this._loading.set(false);
    this.permissionService.loadPermissions();
    this.orgState.loadOrganization();
    this.subState.loadSubscription();
    this.router.navigate(["/"]);
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
