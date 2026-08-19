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
import { I18nService } from "../../i18n/i18n.service";

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
  const container = globalThis.document.createElement("div");
  container.innerHTML = raw;
  const clean = container.textContent ?? "";
  // Limit length to prevent log flooding
  return clean.length > 200 ? clean.substring(0, 200) + "…" : clean;
}

/** Map HTTP status codes to i18n translation keys. */
const STATUS_KEYS: Record<number, string> = {
  400: "error.400",
  401: "error.401",
  402: "error.402",
  403: "error.403",
  404: "error.404",
  408: "error.408",
  409: "error.409",
  422: "error.422",
  428: "error.428_jira",
  429: "error.429",
  500: "error.500",
  502: "error.502",
  503: "error.503",
};

/** English fallbacks used when the i18n key has not been loaded yet. */
const STATUS_FALLBACKS: Record<number, string> = {
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

/**
 * Translate a key via i18n; return null if the key is not yet loaded.
 * This prevents the raw key from being displayed as a message.
 */
function tr(i18n: I18nService, key: string): string | null {
  const result = i18n.t(key);
  return result !== key ? result : null;
}
function isNoDashboard(error: HttpErrorResponse): boolean {
  if (error.status !== 428) return false;
  const msg: string = error.error?.message ?? "";
  return msg.toLowerCase().includes("no active dashboard");
}

/** Detect if a 401/403 is an "email not verified" rejection from the backend. */
function isEmailUnverified(error: HttpErrorResponse): boolean {
  if (error.status !== 401 && error.status !== 403) return false;
  const msg: string = error.error?.message ?? "";
  return (
    msg.toLowerCase().includes("not verified") ||
    msg.toLowerCase().includes("email address not verified") ||
    msg.toLowerCase().includes("verify your email")
  );
}

/** Detect if a 403 is a feature-gating response from the backend. */
function isFeatureGated(error: HttpErrorResponse): boolean {
  const msg = error.error?.message ?? "";
  return (
    error.status === 403 &&
    !isEmailUnverified(error) &&
    (msg.includes("plan does not include") ||
      msg.includes("feature") ||
      msg.includes("FORBIDDEN"))
  );
}

/**
 * Detect Jira's CAPTCHA lockout: a 403 raised after too many failed login
 * attempts, requiring the user to solve a CAPTCHA via the Jira web login
 * before any authentication (including this app) will work again.
 */
function isJiraCaptchaRequired(error: HttpErrorResponse): boolean {
  return error.status === 403 && normalizeErrorCode(error) === "JIRA_CAPTCHA_REQUIRED";
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
  i18n: I18nService,
): Observable<HttpEvent<unknown>> {
  const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);

  if (!refreshToken) {
    performLogout(router);
    return throwError(() =>
      toAppError(
        originalError,
        tr(i18n, "error.session_expired") ??
          "Your session has expired. Please log in again.",
      ),
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
          message =
            tr(i18n, "error.session_revoked") ??
            "Your access has been revoked. Contact your administrator.";
        } else if (status === 401) {
          message =
            tr(i18n, "error.session_expired") ??
            "Your session has expired. Please log in again.";
        } else {
          message =
            tr(i18n, "error.session_ended") ??
            "Your session has ended. Please log in again or contact your administrator if the issue persists.";
        }

        performLogout(router);
        return throwError(() => toAppError(originalError, message));
      }),
    );
}

/**
 * Handles the well-known "special case" error shapes (email unverified,
 * feature gating, Jira CAPTCHA lockout, no active dashboard) that need a
 * distinct propagated shape instead of a generic status-based message.
 * Extracted out of the main catchError callback to keep its complexity low.
 * Returns an Observable to propagate, or null if none of these cases match.
 */
function handleKnownErrorCase(
  error: HttpErrorResponse,
  i18n: I18nService,
): Observable<never> | null {
  if (isEmailUnverified(error)) {
    return throwError(() => ({
      emailUnverified: true,
      status: 403,
      message:
        sanitizeErrorMessage(error.error?.message) ??
        "Email address not verified. Please check your inbox.",
    }));
  }

  if (isFeatureGated(error)) {
    return throwError(() => ({
      featureGated: true,
      status: 403,
      message: error.error?.message ?? "Feature not available on your plan",
    }));
  }

  if (isJiraCaptchaRequired(error)) {
    return throwError(() => ({
      jiraCaptchaRequired: true,
      status: 403,
      message:
        sanitizeErrorMessage(error.error?.message) ??
        "Your Jira account is temporarily locked and requires solving a CAPTCHA. Please log in via your browser first.",
    }));
  }

  if (isNoDashboard(error)) {
    return throwError(() => ({
      noDashboard: true,
      status: 428,
      message:
        tr(i18n, "error.428_dashboard") ??
        "No active dashboard configured. Please select a Jira board in Settings.",
      code: null,
      integrationFailure: false,
      retryable: false,
    }));
  }

  return null;
}

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const httpBackend = inject(HttpBackend);
  const i18n = inject(I18nService);

  // Skip error handling for requests marked as silent (e.g. permission loading)
  if (req.headers.has("X-Silent-Error")) {
    const silentReq = req.clone({
      headers: req.headers.delete("X-Silent-Error"),
    });
    return next(silentReq);
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Known special-case shapes (email verification, feature gating, Jira
      // CAPTCHA lockout, no active dashboard) get a distinct propagated shape.
      const special = handleKnownErrorCase(error, i18n);
      if (special) {
        return special;
      }

      // 428 — Jira account not connected
      if (error.status === 428) {
        return throwError(() =>
          toAppError(
            error,
            tr(i18n, "error.428_jira") ?? STATUS_FALLBACKS[428],
          ),
        );
      }

      // 401 on a protected endpoint: attempt token refresh before giving up.
      // The RETRY_AFTER_REFRESH_HEADER sentinel prevents a second refresh loop.
      if (
        error.status === 401 &&
        !isAuthEndpoint(req.url) &&
        !req.headers.has(RETRY_AFTER_REFRESH_HEADER)
      ) {
        return attemptRefreshAndRetry(
          error,
          req,
          next,
          router,
          httpBackend,
          i18n,
        );
      }

      let message: string;

      if (error.status === 0) {
        message =
          tr(i18n, "error.no_connection") ??
          "Unable to connect to the server. Is the backend running?";
      } else if (isAuthFailure(error, req.url)) {
        localStorage.removeItem("sr_token");
        localStorage.removeItem("sr_refresh_token");
        localStorage.removeItem("sr_token_expiry");
        router.navigate(["/auth/login"]);
        message = tr(i18n, "error.401") ?? STATUS_FALLBACKS[401];
      } else if (error.status === 502 || error.status === 503) {
        message =
          sanitizeErrorMessage(error.error?.message) ??
          (error.status === 503
            ? (tr(i18n, "error.jira_unavailable") ??
              "Jira is temporarily unavailable.")
            : (tr(i18n, "error.jira_token") ??
              "The Jira token is invalid or expired."));
      } else if (isIntegrationUnavailable(error)) {
        message =
          tr(i18n, "error.jira_config") ??
          "Unable to load Jira data. Check your Jira configuration or try again later.";
      } else {
        const sanitized = sanitizeErrorMessage(error.error?.message);
        const statusKey = STATUS_KEYS[error.status];
        message =
          (statusKey ? tr(i18n, statusKey) : null) ??
          sanitized ??
          STATUS_FALLBACKS[error.status] ??
          `Server error: ${error.status}`;
      }

      // Log full error for debugging but never expose to user
      console.error(`[HTTP ${error.status}] ${req.method} ${req.url}`);

      return throwError(() => toAppError(error, message));
    }),
  );
};
