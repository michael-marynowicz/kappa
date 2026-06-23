import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { OrgMember, OrgInvitation } from "./organization.service";

@Injectable({ providedIn: "root" })
export class OrganizationAdminService {
  constructor(private http: HttpClient) {}

  getMembers(orgId: string): Observable<OrgMember[]> {
    return this.http.get<OrgMember[]>(
      `${environment.apiUrl}/admin/organizations/${encodeURIComponent(orgId)}/members`,
    );
  }

  deactivateMember(orgId: string, userId: string): Observable<void> {
    return this.http.patch<void>(
      `${environment.apiUrl}/admin/organizations/${encodeURIComponent(orgId)}/members/${encodeURIComponent(userId)}/deactivate`,
      {},
    );
  }

  reactivateMember(orgId: string, userId: string): Observable<void> {
    return this.http.patch<void>(
      `${environment.apiUrl}/admin/organizations/${encodeURIComponent(orgId)}/members/${encodeURIComponent(userId)}/activate`,
      {},
    );
  }

  removeMember(orgId: string, userId: string): Observable<void> {
    return this.http.delete<void>(
      `${environment.apiUrl}/admin/organizations/${encodeURIComponent(orgId)}/members/${encodeURIComponent(userId)}`,
    );
  }

  getInvitations(orgId: string): Observable<OrgInvitation[]> {
    return this.http.get<OrgInvitation[]>(
      `${environment.apiUrl}/admin/organizations/${encodeURIComponent(orgId)}/invitations`,
    );
  }

  inviteUsers(orgId: string, emails: string[], role: string): Observable<void> {
    return this.http.post<void>(
      `${environment.apiUrl}/admin/organizations/${encodeURIComponent(orgId)}/invitations`,
      { emails, role },
    );
  }

  revokeInvitation(orgId: string, invitationId: string): Observable<void> {
    return this.http.delete<void>(
      `${environment.apiUrl}/admin/organizations/${encodeURIComponent(orgId)}/invitations/${encodeURIComponent(invitationId)}`,
    );
  }

  resendInvitation(orgId: string, invitationId: string): Observable<void> {
    return this.http.post<void>(
      `${environment.apiUrl}/admin/organizations/${encodeURIComponent(orgId)}/invitations/${encodeURIComponent(invitationId)}/resend`,
      {},
    );
  }

  deleteOrganization(orgId: string): Observable<void> {
    return this.http.delete<void>(
      `${environment.apiUrl}/admin/organizations/${encodeURIComponent(orgId)}`,
    );
  }
}
