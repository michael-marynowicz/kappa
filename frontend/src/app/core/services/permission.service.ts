import { Injectable, inject, signal, computed } from "@angular/core";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import {
  UserPermissions,
  PlanCode,
  FeatureGateState,
} from "../models/permission.model";
import { environment } from "../../../environments/environment";

@Injectable({ providedIn: "root" })
export class PermissionService {
  private readonly http = inject(HttpClient);

  private readonly _permissions = signal<UserPermissions | null>(null);
  private readonly _loaded = signal(false);

  readonly permissions = this._permissions.asReadonly();
  readonly loaded = this._loaded.asReadonly();

  readonly plan = computed<PlanCode | null>(
    () => this._permissions()?.plan ?? null,
  );
  readonly isPremium = computed(() => this._permissions()?.plan === "PREMIUM");

  /**
   * Load permissions from the backend.
   * Should be called once after authentication.
   */
  loadPermissions(): void {
    const headers = new HttpHeaders().set("X-Silent-Error", "true");
    this.http
      .get<UserPermissions>(
        `${environment.apiBaseUrl}/api/v1/users/me/permissions`,
        { headers },
      )
      .subscribe({
        next: (perms) => {
          this._permissions.set(perms);
          this._loaded.set(true);
        },
        error: () => {
          // Fallback: no permissions loaded, everything gated
          this._permissions.set({ plan: "FREE", permissions: [] });
          this._loaded.set(true);
        },
      });
  }

  /**
   * Check if the user has a specific permission.
   */
  hasPermission(permission: string): boolean {
    const perms = this._permissions();
    if (!perms) return false;
    return perms.permissions.includes(permission);
  }

  /**
   * Reactive signal-based check for use in templates/computed.
   */
  hasPermissionSignal(permission: string) {
    return computed(() => {
      const perms = this._permissions();
      if (!perms) return false;
      return perms.permissions.includes(permission);
    });
  }

  /**
   * Determine the gate state for a feature.
   * - If user has the permission → 'enabled'
   * - If `hideWhenDenied` is true → 'hidden'
   * - Otherwise → 'disabled' (show with premium overlay)
   */
  getFeatureState(
    permission: string,
    hideWhenDenied = false,
  ): FeatureGateState {
    if (this.hasPermission(permission)) return "enabled";
    return hideWhenDenied ? "hidden" : "disabled";
  }

  /**
   * Reactive computed for feature gate state.
   */
  featureState(permission: string, hideWhenDenied = false) {
    return computed<FeatureGateState>(() => {
      const perms = this._permissions();
      if (!perms) return hideWhenDenied ? "hidden" : "disabled";
      if (perms.permissions.includes(permission)) return "enabled";
      return hideWhenDenied ? "hidden" : "disabled";
    });
  }

  /**
   * Clear permissions (e.g. on logout).
   */
  clear(): void {
    this._permissions.set(null);
    this._loaded.set(false);
  }
}
