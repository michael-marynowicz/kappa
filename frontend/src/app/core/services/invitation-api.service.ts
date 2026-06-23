import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import {
  Invitation,
  CreateInvitationRequest,
  BulkInviteRequest,
  BulkInviteResult,
  InvitationCheck,
  VerifyEmailResponse,
} from "../models/invitation.model";
import { environment } from "../../../environments/environment";

@Injectable({ providedIn: "root" })
export class InvitationApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/api/v1`;

  getInvitations(): Observable<Invitation[]> {
    return this.http.get<Invitation[]>(
      `${this.baseUrl}/organization/invitations`,
    );
  }

  createInvitation(request: CreateInvitationRequest): Observable<Invitation> {
    return this.http.post<Invitation>(
      `${this.baseUrl}/organization/invitations`,
      request,
    );
  }

  bulkInvite(request: BulkInviteRequest): Observable<BulkInviteResult> {
    return this.http.post<BulkInviteResult>(
      `${this.baseUrl}/organization/invitations/bulk`,
      request,
    );
  }

  revokeInvitation(id: string): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/organization/invitations/${encodeURIComponent(id)}`,
    );
  }

  checkInvitation(email: string): Observable<InvitationCheck> {
    return this.http.get<InvitationCheck>(`${this.baseUrl}/invitations/check`, {
      params: { email },
    });
  }

  verifyEmail(token: string): Observable<VerifyEmailResponse> {
    return this.http.post<VerifyEmailResponse>(
      `${this.baseUrl}/auth/verify-email`,
      null,
      { params: { token } },
    );
  }

  resendVerification(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/auth/resend-verification`, {});
  }
}
