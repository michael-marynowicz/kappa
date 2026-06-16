import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import {
  Organization,
  OrganizationMember,
  InviteMemberRequest,
} from "../models/organization.model";
import { environment } from "../../../environments/environment";

@Injectable({ providedIn: "root" })
export class OrganizationApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/api/v1/organization`;

  getCurrent(): Observable<Organization> {
    return this.http.get<Organization>(`${this.baseUrl}/current`);
  }

  getMembers(): Observable<OrganizationMember[]> {
    return this.http.get<OrganizationMember[]>(`${this.baseUrl}/members`);
  }

  inviteMember(request: InviteMemberRequest): Observable<OrganizationMember> {
    return this.http.post<OrganizationMember>(
      `${this.baseUrl}/members`,
      request,
    );
  }

  removeMember(userId: string): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/members/${encodeURIComponent(userId)}`,
    );
  }
}
