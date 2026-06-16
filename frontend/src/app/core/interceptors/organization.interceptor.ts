import { HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { OrganizationStateService } from "../services/organization-state.service";

export const organizationInterceptor: HttpInterceptorFn = (req, next) => {
  const orgState = inject(OrganizationStateService);
  const org = orgState.organization();

  if (org) {
    const orgReq = req.clone({
      setHeaders: {
        "X-Organization-Id": org.id,
      },
    });
    return next(orgReq);
  }

  return next(req);
};
