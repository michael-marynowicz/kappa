import { HttpRequest, HttpResponse } from "@angular/common/http";
import { of } from "rxjs";
import { securityInterceptor } from "./security.interceptor";

describe("securityInterceptor", () => {
  it("should add X-Content-Type-Options header", (done) => {
    const req = new HttpRequest("GET", "/api/test");

    const next = (r: HttpRequest<unknown>) => {
      expect(r.headers.get("X-Content-Type-Options")).toBe("nosniff");
      return of(new HttpResponse());
    };

    securityInterceptor(req, next).subscribe(() => done());
  });

  it("should add X-Requested-With header", (done) => {
    const req = new HttpRequest("GET", "/api/test");

    const next = (r: HttpRequest<unknown>) => {
      expect(r.headers.get("X-Requested-With")).toBe("XMLHttpRequest");
      return of(new HttpResponse());
    };

    securityInterceptor(req, next).subscribe(() => done());
  });

  it("should add Cache-Control no-store header", (done) => {
    const req = new HttpRequest("GET", "/api/data");

    const next = (r: HttpRequest<unknown>) => {
      expect(r.headers.get("Cache-Control")).toBe("no-store");
      return of(new HttpResponse());
    };

    securityInterceptor(req, next).subscribe(() => done());
  });

  it("should add Pragma no-cache header", (done) => {
    const req = new HttpRequest("POST", "/api/update", {});

    const next = (r: HttpRequest<unknown>) => {
      expect(r.headers.get("Pragma")).toBe("no-cache");
      return of(new HttpResponse());
    };

    securityInterceptor(req, next).subscribe(() => done());
  });

  it("should not modify the original request", (done) => {
    const req = new HttpRequest("GET", "/api/test");

    const next = (r: HttpRequest<unknown>) => {
      expect(r).not.toBe(req); // Should be a clone
      expect(req.headers.has("X-Requested-With")).toBeFalse();
      return of(new HttpResponse());
    };

    securityInterceptor(req, next).subscribe(() => done());
  });
});
