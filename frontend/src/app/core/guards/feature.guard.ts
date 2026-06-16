import { inject } from "@angular/core";
import { CanActivateFn, Router } from "@angular/router";
import { PermissionService } from "../services/permission.service";

/**
 * Route guard that checks if the user has the required permission.
 * Uses the locally-cached permissions from PermissionService (no HTTP call).
 */
export function featureGuard(featureCode: string): CanActivateFn {
  return () => {
    const permissionService = inject(PermissionService);
    const router = inject(Router);

    if (permissionService.hasPermission(featureCode)) {
      return true;
    }

    return router.createUrlTree(["/settings/billing"]);
  };
}
