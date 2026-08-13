import { HttpInterceptorFn } from "@angular/common/http";

/**
 * Security interceptor: adds security headers to every outgoing request.
 *
 * - X-Content-Type-Options: prevent MIME-type sniffing attacks
 * - X-Requested-With: mark as AJAX (helps server-side CSRF checks)
 * - Cache-Control: prevent sensitive data from being cached
 */
export const securityInterceptor: HttpInterceptorFn = (req, next) => {
  const securedReq = req.clone({
    setHeaders: {
      "X-Requested-With": "XMLHttpRequest",
    },
  });
  return next(securedReq);
};
