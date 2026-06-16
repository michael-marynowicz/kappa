import {
  HttpErrorResponse,
  HttpRequest,
  HttpResponse,
} from "@angular/common/http";
import { of, throwError } from "rxjs";
import { errorInterceptor } from "./error.interceptor";

describe("errorInterceptor", () => {
  const dummyReq = new HttpRequest("GET", "/api/test");

  beforeEach(() => {
    spyOn(console, "error"); // suppress console.error in test output
  });

  it("should pass through successful responses", (done) => {
    const next = () => of(new HttpResponse({ body: { ok: true } }));

    errorInterceptor(dummyReq, next).subscribe((res) => {
      expect(res).toBeTruthy();
      done();
    });
  });

  it("should map status 0 to connection error message", (done) => {
    const next = () =>
      throwError(
        () => new HttpErrorResponse({ status: 0, statusText: "Unknown" }),
      );

    errorInterceptor(dummyReq, next).subscribe({
      error: (err) => {
        expect(err.message).toBe(
          "Unable to connect to the server. Is the backend running?",
        );
        done();
      },
    });
  });

  it("should use mapped status message for known HTTP status codes", (done) => {
    const next = () =>
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            statusText: "Bad Request",
            error: { message: "Validation failed" },
          }),
      );

    errorInterceptor(dummyReq, next).subscribe({
      error: (err) => {
        expect(err.message).toBe("Invalid request. Please check your input.");
        done();
      },
    });
  });

  it("should fall back to sanitized server message for unmapped status", (done) => {
    const next = () =>
      throwError(
        () =>
          new HttpErrorResponse({
            status: 418,
            statusText: "I'm a Teapot",
            error: { message: "Custom backend error" },
          }),
      );

    errorInterceptor(dummyReq, next).subscribe({
      error: (err) => {
        expect(err.message).toBe("Custom backend error");
        done();
      },
    });
  });

  it("should use generic message for 500 without exposing server details", (done) => {
    const next = () =>
      throwError(
        () =>
          new HttpErrorResponse({
            status: 500,
            statusText: "Internal Server Error",
          }),
      );

    errorInterceptor(dummyReq, next).subscribe({
      error: (err) => {
        expect(err.message).toBe("An internal server error occurred.");
        done();
      },
    });
  });

  it("should strip HTML tags from error messages to prevent XSS", (done) => {
    const next = () =>
      throwError(
        () =>
          new HttpErrorResponse({
            status: 418,
            statusText: "Teapot",
            error: { message: '<script>alert("xss")</script>Bad input' },
          }),
      );

    errorInterceptor(dummyReq, next).subscribe({
      error: (err) => {
        expect(err.message).not.toContain("<script>");
        expect(err.message).toContain("Bad input");
        done();
      },
    });
  });

  it("should truncate very long error messages", (done) => {
    const longMessage = "A".repeat(300);
    const next = () =>
      throwError(
        () =>
          new HttpErrorResponse({
            status: 418,
            statusText: "Teapot",
            error: { message: longMessage },
          }),
      );

    errorInterceptor(dummyReq, next).subscribe({
      error: (err) => {
        expect(err.message.length).toBeLessThanOrEqual(201); // 200 + ellipsis
        done();
      },
    });
  });
});
