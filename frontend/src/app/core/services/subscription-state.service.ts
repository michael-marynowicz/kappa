import { Injectable, inject, signal, computed } from "@angular/core";
import { SubscriptionApiService } from "./subscription-api.service";
import { Observable, of } from "rxjs";
import { catchError, map, tap } from "rxjs/operators";
import {
  Subscription,
  Plan,
  SubscriptionStatus,
} from "../models/subscription.model";

@Injectable({ providedIn: "root" })
export class SubscriptionStateService {
  private readonly api = inject(SubscriptionApiService);

  private readonly _subscription = signal<Subscription | null>(null);
  private readonly _plans = signal<Plan[]>([]);
  private readonly _features = signal<string[]>([]);
  private readonly _loading = signal(false);
  private readonly _error = signal<string | null>(null);

  readonly subscription = this._subscription.asReadonly();
  readonly plans = this._plans.asReadonly();
  readonly features = this._features.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();

  readonly isActive = computed(() => {
    const sub = this._subscription();
    return sub?.status === "ACTIVE" || sub?.status === "TRIALING";
  });

  readonly status = computed<SubscriptionStatus>(() => {
    return this._subscription()?.status ?? "NONE";
  });

  resolveActiveAccess(): Observable<boolean> {
    const existing = this._subscription();
    if (existing) {
      return of(existing.status === "ACTIVE" || existing.status === "TRIALING");
    }

    return this.api.getCurrent().pipe(
      tap((sub) => this._subscription.set(sub)),
      map((sub) => sub.status === "ACTIVE" || sub.status === "TRIALING"),
      catchError(() => of(false)),
    );
  }

  loadSubscription(): void {
    this._loading.set(true);
    this.api.getCurrent().subscribe({
      next: (sub) => {
        this._subscription.set(sub);
        this._loading.set(false);
      },
      error: (err) => {
        this._error.set(err.message ?? "Failed to load subscription");
        this._loading.set(false);
      },
    });
  }

  loadPlans(): void {
    this.api.getPlans().subscribe({
      next: (plans) => this._plans.set(plans),
      error: (err) => this._error.set(err.message ?? "Failed to load plans"),
    });
  }

  loadFeatures(): void {
    // Features are checked per-code via featureGuard; no bulk list endpoint
  }

  subscribe(planCode: string): void {
    this._loading.set(true);
    this.api.changePlan({ planCode }).subscribe({
      next: (sub) => {
        this._subscription.set(sub);
        this._loading.set(false);
      },
      error: (err) => {
        this._error.set(err.message ?? "Subscription failed");
        this._loading.set(false);
      },
    });
  }

  startCheckout(planCode: string): void {
    this._loading.set(true);
    this.api.createCheckoutSession(planCode).subscribe({
      next: (session) => {
        this._loading.set(false);
        globalThis.location.href = session.checkoutUrl;
      },
      error: (err) => {
        this._error.set(err.message ?? "Failed to start checkout");
        this._loading.set(false);
      },
    });
  }

  redeem(promoCode: string): void {
    this._loading.set(true);
    this.api.redeem({ promoCode }).subscribe({
      next: (sub) => {
        this._subscription.set(sub);
        this._loading.set(false);
      },
      error: (err) => {
        this._error.set(err.message ?? "Redemption failed");
        this._loading.set(false);
      },
    });
  }

  cancel(): void {
    this._loading.set(true);
    this.api.cancel().subscribe({
      next: () => {
        this._subscription.update((s) =>
          s
            ? { ...s, status: "CANCELED" as const, cancelAtPeriodEnd: true }
            : null,
        );
        this._loading.set(false);
      },
      error: (err) => {
        this._error.set(err.message ?? "Cancellation failed");
        this._loading.set(false);
      },
    });
  }

  hasFeature(code: string): boolean {
    return this._features().includes(code);
  }

  clearError(): void {
    this._error.set(null);
  }

  clear(): void {
    this._subscription.set(null);
    this._plans.set([]);
    this._features.set([]);
    this._loading.set(false);
    this._error.set(null);
  }
}
