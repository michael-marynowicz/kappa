import { inject } from "@angular/core";
import { CanActivateFn, Router } from "@angular/router";
import { map } from "rxjs/operators";
import { SubscriptionStateService } from "../services/subscription-state.service";

export const subscriptionGuard: CanActivateFn = () => {
  const subState = inject(SubscriptionStateService);
  const router = inject(Router);

  return subState
    .resolveActiveAccess()
    .pipe(
      map((active) =>
        active ? true : router.createUrlTree(["/settings/billing"]),
      ),
    );
};

/**
 * Guard that restricts the billing settings route to SELF_SERVICE organizations only.
 * Enterprise and Pilot orgs are redirected to settings/jira.
 */
export const billingRouteGuard: CanActivateFn = () => {
  const subState = inject(SubscriptionStateService);
  const router = inject(Router);

  if (!subState.showPaymentPages()) {
    return router.createUrlTree(["/settings/jira"]);
  }
  return true;
};
