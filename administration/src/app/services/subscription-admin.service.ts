import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";

@Injectable({ providedIn: "root" })
export class SubscriptionAdminService {
  constructor(private http: HttpClient) {}

  assignEnterprise(organizationId: string, planCode: string): Observable<void> {
    return this.http.post<void>(
      `${environment.apiUrl}/subscription/admin/enterprise`,
      { organizationId, planCode },
    );
  }

  assignPilot(
    organizationId: string,
    planCode: string,
    pilotExpiresAt: string,
  ): Observable<void> {
    return this.http.post<void>(
      `${environment.apiUrl}/subscription/admin/pilot`,
      { organizationId, planCode, pilotExpiresAt },
    );
  }

  convertToSelfService(organizationId: string): Observable<void> {
    return this.http.post<void>(
      `${environment.apiUrl}/subscription/admin/convert-pilot`,
      { organizationId },
    );
  }

  setActivation(organizationId: string, active: boolean): Observable<void> {
    return this.http.post<void>(
      `${environment.apiUrl}/subscription/admin/activation?active=${active}`,
      { organizationId },
    );
  }
}
