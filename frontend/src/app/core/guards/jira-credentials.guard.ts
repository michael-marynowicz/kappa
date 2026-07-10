import { inject } from "@angular/core";
import { CanActivateFn, Router } from "@angular/router";
import { map } from "rxjs/operators";
import { AuthStateService } from "../services/auth-state.service";
import { JiraCredentialsStateService } from "../services/jira-credentials-state.service";

/**
 * Guard that ensures non-ADMIN users have connected their personal Jira account
 * before accessing data routes (sprint, backlog, metrics, capacity).
 *
 * ADMIN users bypass this check — they use the organisation-level Jira config.
 */
export const jiraCredentialsGuard: CanActivateFn = () => {
  const authState = inject(AuthStateService);
  const jiraCreds = inject(JiraCredentialsStateService);
  const router = inject(Router);

  // ADMIN relies on org-level credentials — no personal credentials required
  if (authState.user()?.role === "ADMIN") {
    return true;
  }

  // If already loaded and connected, let through immediately
  const cached = jiraCreds.credentials();
  if (cached !== null) {
    return cached.connected
      ? true
      : router.createUrlTree(["/settings/jira"]);
  }

  // Fetch from backend then decide
  return jiraCreds
    .load()
    .pipe(
      map((creds) =>
        creds.connected ? true : router.createUrlTree(["/settings/jira"]),
      ),
    );
};
