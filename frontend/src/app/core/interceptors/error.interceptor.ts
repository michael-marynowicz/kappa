import {
  HttpInterceptorFn,
  HttpErrorResponse,
  HttpBackend,
  HttpClient,
  HttpHandlerFn,
  HttpRequest,
  HttpEvent,
} from "@angular/common/http";
import { inject } from "@angular/core";
import { Router } from "@angular/router";
import { Observable, catchError, switchMap, throwError } from "rxjs";
import { AuthResponse } from "../models/user.model";
import { environment } from "../../../environments/environment";

const AUTH_ENDPOINT_PATTERNS = ["/api/v1/auth", "/api/v1/session"];
const AUTH_FAILURE_CODES = new Set(["AUTH_EXPIRED", "AUTH_INVALID"]);
const INTEGRATION_UNAVAILABLE_STATUS = new Set([424, 502, 503, 504]);
/** Sentinel header added to retry requests to prevent infinite refresh loops. */
const RETRY_AFTER_REFRESH_HEADER = "X-Retry-After-Refresh";
const TOKEN_KEY = "sr_token";
const REFRESH_TOKEN_KEY = "sr_refresh_token";
const TOKEN_EXPIRY_KEY = "sr_token_expiry";

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
  402: "Plan limit reached. Upgrade your plan to continue.",
  403: "You do not have permission to perform this action.",
  404: "The requested resource was not found.",
  408: "The request timed out. Please try again.",
  409: "A conflict occurred. The data may have been modified.",
  422: "The data provided is invalid.",
  428: "Your Jira account is not connected. Please configure your Jira credentials.",
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

function performLogout(router: Router): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(TOKEN_EXPIRY_KEY);
  router.navigate(["/auth/login"]);
}

/**
 * Attempts POST /auth/refresh after receiving a 401 on a protected endpoint.
 * On success  → stores the new tokens and retries the original request.
 * On failure  → logs out and throws with a message that distinguishes between
 *               a naturally-expired session (401) and a revoked account (404).
 *
 * Uses HttpBackend directly to bypass the interceptor chain and avoid
 * circular-dependency issues with HttpClient.
 */
function attemptRefreshAndRetry(
  originalError: HttpErrorResponse,
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
  router: Router,
  httpBackend: HttpBackend,
): Observable<HttpEvent<unknown>> {
  const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);

  if (!refreshToken) {
    performLogout(router);
    return throwError(() =>
      toAppError(originalError, "Votre session a expiré. Reconnectez-vous."),
    );
  }

  const http = new HttpClient(httpBackend);
  return http
    .post<AuthResponse>(`${environment.apiBaseUrl}/api/v1/auth/refresh`, {
      refreshToken,
    })
    .pipe(
      switchMap((res) => {
        localStorage.setItem(TOKEN_KEY, res.accessToken);
        localStorage.setItem(REFRESH_TOKEN_KEY, res.refreshToken);
        localStorage.setItem(
          TOKEN_EXPIRY_KEY,
          String(Date.now() + res.expiresIn * 1000),
        );
        // Retry the original request with the new token.
        // The sentinel header prevents a second refresh attempt if this retry also fails.
        const retryReq = req.clone({
          headers: req.headers
            .set("Authorization", `Bearer ${res.accessToken}`)
            .set(RETRY_AFTER_REFRESH_HEADER, "1"),
        });
        return next(retryReq);
      }),
      catchError((refreshError: unknown) => {
        const status =
          refreshError instanceof HttpErrorResponse ? refreshError.status : 0;

        let message: string;
        if (status === 404) {
          // Refresh token not found in DB → account was deleted / revoked
          message =
            "Votre accès a été révoqué. Contactez votre administrateur.";
        } else if (status === 401) {
          // Refresh token invalid or naturally expired
          message = "Votre session a expiré. Reconnectez-vous.";
        } else {
          // Network error or unexpected status — stay neutral
          message =
            "Votre session a pris fin. Reconnectez-vous ou contactez votre administrateur si le problème persiste.";
        }

        performLogout(router);
        return throwError(() => toAppError(originalError, message));
      }),
    );
}

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const httpBackend = inject(HttpBackend);

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

      // 428 — Jira account not connected: surface the error, let the UI banner handle guidance
      if (error.status === 428) {
        return throwError(() => toAppError(error, STATUS_MESSAGES[428]));
      }

      // 401 on a protected endpoint: attempt token refresh before giving up.
      // The RETRY_AFTER_REFRESH_HEADER sentinel prevents a second refresh loop.
      if (
        error.status === 401 &&
        !isAuthEndpoint(req.url) &&
        !req.headers.has(RETRY_AFTER_REFRESH_HEADER)
      ) {
        return attemptRefreshAndRetry(error, req, next, router, httpBackend);
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
      } else if (error.status === 502 || error.status === 503) {
        message =
          sanitizeErrorMessage(error.error?.message) ??
          (error.status === 503
            ? "Jira est temporairement inaccessible"
            : "Le token Jira est invalide ou expire.");
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
