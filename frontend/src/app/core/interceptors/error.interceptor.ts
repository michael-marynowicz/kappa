import { HttpInterceptorFn, HttpErrorResponse } from "@angular/common/http";
import { inject } from "@angular/core";
import { Router } from "@angular/router";
import { catchError, throwError } from "rxjs";

const AUTH_ENDPOINT_PATTERNS = ["/api/v1/auth", "/api/v1/session"];
const AUTH_FAILURE_CODES = new Set(["AUTH_EXPIRED", "AUTH_INVALID"]);
const INTEGRATION_UNAVAILABLE_STATUS = new Set([424, 502, 503, 504]);

/**
 * Functional HTTP interceptor: standardizes error handling across all API calls.
 * Maps HttpErrorResponse to human-readable error messages.
 * Handles 401 → redirect to login.
 */
/** Sanitize server error messages to avoid leaking sensitive backend details. */
function sanitizeErrorMessage(raw: string | undefined | null): string | null {
  if (!raw || typeof raw !== "string") return null;
  // Strip HTML tags to prevent reflected XSS from error messages
  const clean = raw.replace(/<[^>]*>/g, "");
  // Limit length to prevent log flooding
  return clean.length > 200 ? clean.substring(0, 200) + "…" : clean;
}

/** Map HTTP status codes to safe, user-friendly messages. */
const STATUS_MESSAGES: Record<number, string> = {
  400: "Invalid request. Please check your input.",
  401: "Authentication required. Please log in.",
  403: "You do not have permission to perform this action.",
  404: "The requested resource was not found.",
  408: "The request timed out. Please try again.",
  409: "A conflict occurred. The data may have been modified.",
  422: "The data provided is invalid.",
  429: "Too many requests. Please wait before trying again.",
  500: "An internal server error occurred.",
  502: "The server is temporarily unavailable.",
  503: "The service is undergoing maintenance.",
};

/** Detect if a 403 is a feature-gating response from the backend. */
function isFeatureGated(error: HttpErrorResponse): boolean {
  const msg = error.error?.message ?? "";
  return (
    error.status === 403 &&
    (msg.includes("plan does not include") ||
      msg.includes("feature") ||
      msg.includes("FORBIDDEN"))
  );
}

function normalizeErrorCode(error: HttpErrorResponse): string | null {
  const code =
    error.error?.code ?? error.error?.errorCode ?? error.error?.error ?? null;
  return typeof code === "string" ? code.toUpperCase() : null;
}

function isAuthEndpoint(url: string): boolean {
  return AUTH_ENDPOINT_PATTERNS.some((pattern) => url.includes(pattern));
}

function isAuthFailure(error: HttpErrorResponse, requestUrl: string): boolean {
  const errorCode = normalizeErrorCode(error);
  if (errorCode && AUTH_FAILURE_CODES.has(errorCode)) {
    return true;
  }

  return (
    (error.status === 401 || error.status === 403) && isAuthEndpoint(requestUrl)
  );
}

function isIntegrationUnavailable(error: HttpErrorResponse): boolean {
  return INTEGRATION_UNAVAILABLE_STATUS.has(error.status);
}

function toAppError(
  error: HttpErrorResponse,
  message: string,
): {
  status: number;
  code: string | null;
  message: string;
  integrationFailure: boolean;
  retryable: boolean;
} {
  return {
    status: error.status,
    code: normalizeErrorCode(error),
    message,
    integrationFailure: isIntegrationUnavailable(error),
    retryable: isIntegrationUnavailable(error),
  };
}

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  // Skip error handling for requests marked as silent (e.g. permission loading)
  if (req.headers.has("X-Silent-Error")) {
    const silentReq = req.clone({
      headers: req.headers.delete("X-Silent-Error"),
    });
    return next(silentReq);
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Feature-gated 403: propagate silently so components can handle gracefully
      if (isFeatureGated(error)) {
        return throwError(() => ({
          featureGated: true,
          status: 403,
          message: error.error?.message ?? "Feature not available on your plan",
        }));
      }

      let message: string;

      if (error.status === 0) {
        message = "Unable to connect to the server. Is the backend running?";
      } else if (isAuthFailure(error, req.url)) {
        // Strict logout policy: only auth/session failures or explicit AUTH_* codes.
        localStorage.removeItem("sr_token");
        localStorage.removeItem("sr_refresh_token");
        localStorage.removeItem("sr_token_expiry");
        router.navigate(["/auth/login"]);
        message = STATUS_MESSAGES[401];
      } else if (isIntegrationUnavailable(error)) {
        message =
          "Impossible de charger les donnees Jira. Verifie la configuration Jira (token/base URL/projet) ou reessaie plus tard.";
      } else {
        // Backend error format: { status, error, message, details[], timestamp }
        const sanitized = sanitizeErrorMessage(error.error?.message);
        message =
          sanitized ??
          STATUS_MESSAGES[error.status] ??
          `Server error: ${error.status}`;
      }

      // Log full error for debugging but never expose to user
      console.error(`[HTTP ${error.status}] ${req.method} ${req.url}`);

      return throwError(() => toAppError(error, message));
    }),
  );
};
