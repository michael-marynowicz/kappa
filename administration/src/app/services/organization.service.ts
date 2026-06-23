import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";

export interface Organization {
  id: string;
  name: string;
  slug: string;
  email: string;
  active: boolean;
}

export interface Subscription {
  id: string;
  planCode: "FREE" | "PRO" | "BUSINESS";
  planName: string;
  status: "ACTIVE" | "TRIALING" | "PAST_DUE" | "CANCELED" | "EXPIRED";
  billingProvider: "NONE" | "STRIPE" | "MANUAL";
  subscriptionType: "SELF_SERVICE" | "ENTERPRISE" | "PILOT";
  pilotExpiresAt: string | null;
  showPaymentPages: boolean;
  isEnterprise: boolean;
  isPilot: boolean;
  currentPeriodStart: string;
  currentPeriodEnd: string | null;
  cancelAtPeriodEnd: boolean;
}

export interface OrgAdminSummary {
  id: string;
  name: string;
  slug: string;
  email: string;
  active: boolean;
  createdAt: string;
  lastActivityAt: string | null;
  userCount: number;
  pendingInvitationCount: number;
  subscriptionType: "SELF_SERVICE" | "ENTERPRISE" | "PILOT" | null;
  subscriptionStatus:
    | "ACTIVE"
    | "TRIALING"
    | "PAST_DUE"
    | "CANCELED"
    | "EXPIRED"
    | null;
  planCode: "FREE" | "PRO" | "BUSINESS" | null;
  pilotExpiresAt: string | null;
}

export interface OrgAdminDetail {
  id: string;
  name: string;
  slug: string;
  email: string;
  active: boolean;
  createdAt: string;
  lastActivityAt: string | null;
  userCount: number;
  pendingInvitationCount: number;
  subscription: Subscription | null;
}

export interface OrgMember {
  id: string;
  name: string;
  email: string;
  role: string;
  lastLoginAt: string | null;
  status: "ACTIVE" | "INACTIVE";
}

export interface OrgInvitation {
  id: string;
  email: string;
  role: string;
  createdAt: string;
}

@Injectable({ providedIn: "root" })
export class OrganizationService {
  constructor(private http: HttpClient) {}

  getOrganizations(): Observable<Organization[]> {
    return this.http.get<Organization[]>(`${environment.apiUrl}/organizations`);
  }

  getSubscription(organizationId: string): Observable<Subscription> {
    return this.http.get<Subscription>(
      `${environment.apiUrl}/subscription?organizationId=${encodeURIComponent(organizationId)}`,
    );
  }

  getAdminOrganizations(): Observable<OrgAdminSummary[]> {
    return this.http.get<OrgAdminSummary[]>(
      `${environment.apiUrl}/admin/organizations`,
    );
  }

  getAdminOrganizationDetail(id: string): Observable<OrgAdminDetail> {
    return this.http.get<OrgAdminDetail>(
      `${environment.apiUrl}/admin/organizations/${encodeURIComponent(id)}`,
    );
  }
}
