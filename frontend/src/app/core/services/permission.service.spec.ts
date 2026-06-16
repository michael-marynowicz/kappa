import { TestBed } from "@angular/core/testing";
import {
  HttpClientTestingModule,
  HttpTestingController,
} from "@angular/common/http/testing";
import { PermissionService } from "./permission.service";
import { environment } from "../../../environments/environment";

describe("PermissionService", () => {
  let service: PermissionService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    service = TestBed.inject(PermissionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });

  it("should load permissions from backend", () => {
    service.loadPermissions();

    const req = httpMock.expectOne(
      `${environment.apiBaseUrl}/api/v1/users/me/permissions`,
    );
    expect(req.request.method).toBe("GET");
    req.flush({
      plan: "FREE",
      permissions: ["US_TABLE_VIEW", "METRICS_BASIC"],
    });

    expect(service.loaded()).toBe(true);
    expect(service.plan()).toBe("FREE");
    expect(service.hasPermission("US_TABLE_VIEW")).toBe(true);
    expect(service.hasPermission("EXPORT_CSV")).toBe(false);
  });

  it("should report isPremium correctly", () => {
    service.loadPermissions();
    const req = httpMock.expectOne(
      `${environment.apiBaseUrl}/api/v1/users/me/permissions`,
    );
    req.flush({
      plan: "PREMIUM",
      permissions: ["US_TABLE_VIEW", "EXPORT_CSV"],
    });

    expect(service.isPremium()).toBe(true);
  });

  it("should fallback gracefully on error", () => {
    service.loadPermissions();
    const req = httpMock.expectOne(
      `${environment.apiBaseUrl}/api/v1/users/me/permissions`,
    );
    req.error(new ProgressEvent("error"));

    expect(service.loaded()).toBe(true);
    expect(service.plan()).toBe("FREE");
    expect(service.hasPermission("US_TABLE_VIEW")).toBe(false);
  });

  it("should return correct feature state", () => {
    service.loadPermissions();
    const req = httpMock.expectOne(
      `${environment.apiBaseUrl}/api/v1/users/me/permissions`,
    );
    req.flush({ plan: "FREE", permissions: ["US_TABLE_VIEW"] });

    expect(service.getFeatureState("US_TABLE_VIEW")).toBe("enabled");
    expect(service.getFeatureState("EXPORT_CSV")).toBe("disabled");
    expect(service.getFeatureState("EXPORT_CSV", true)).toBe("hidden");
  });

  it("should clear permissions on logout", () => {
    service.loadPermissions();
    const req = httpMock.expectOne(
      `${environment.apiBaseUrl}/api/v1/users/me/permissions`,
    );
    req.flush({ plan: "FREE", permissions: ["US_TABLE_VIEW"] });

    service.clear();
    expect(service.loaded()).toBe(false);
    expect(service.hasPermission("US_TABLE_VIEW")).toBe(false);
  });
});
