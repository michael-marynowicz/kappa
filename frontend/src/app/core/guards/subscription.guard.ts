import { inject } from "@angular/core";
import { CanActivateFn, Router } from "@angular/router";
import { map } from "rxjs/operators";
import { SubscriptionStateService } from "../services/subscription-state.service";
import { AuthStateService } from "../services/auth-state.service";

export const subscriptionGuard: CanActivateFn = () => {
  const subState = inject(SubscriptionStateService);
  const authState = inject(AuthStateService);
  const router = inject(Router);

  // Non-admins don't have access to the subscription API — allow access unconditionally
  if (authState.user()?.role !== "ADMIN") {
    return true;
  }

  return subState
    .resolveActiveAccess()
    .pipe(
      map((active) =>
        active ? true : router.createUrlTree(["/settings/billing"]),
      ),
    );
};

/**
 * Guard that restricts the billing settings route to SELF_SERVICE organizations only
 * AND to ADMIN users only. Non-admins and Enterprise/Pilot orgs are redirected.
 */
export const billingRouteGuard: CanActivateFn = () => {
  const subState = inject(SubscriptionStateService);
  const authState = inject(AuthStateService);
  const router = inject(Router);

  if (authState.user()?.role !== "ADMIN") {
    return router.createUrlTree(["/settings/jira"]);
  }

  if (!subState.showPaymentPages()) {
    return router.createUrlTree(["/settings/jira"]);
  }
  return true;
};
