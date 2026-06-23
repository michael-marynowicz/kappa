import { HttpErrorResponse, HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { catchError, throwError } from "rxjs";
import { MatSnackBar } from "@angular/material/snack-bar";
import { AuthService } from "../services/auth.service";

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        authService.logout();
        snackBar.open("Session expired. Please log in again.", "Close", {
          duration: 4000,
          panelClass: ["snackbar-error"],
        });
      } else if (error.status === 404) {
        // 404 is not necessarily an error (e.g., no organizations found)
        // Let the component handle it silently
      } else if (error.status >= 400) {
        const message: string =
          (error.error as { message?: string })?.message ??
          error.message ??
          "An unexpected error occurred.";
        snackBar.open(`Error ${error.status}: ${message}`, "Close", {
          duration: 5000,
          panelClass: ["snackbar-error"],
        });
      }
      return throwError(() => error);
    }),
  );
};
