export interface InvitationDashboard {
  id: string;
  name: string;
}

export interface Invitation {
  id: string;
  email: string;
  role: string;
  status: "PENDING" | "ACCEPTED" | "EXPIRED";
  createdAt: string;
  dashboardIds?: string[];
  dashboards?: InvitationDashboard[];
}

export interface CreateInvitationRequest {
  email: string;
  role: string;
  dashboardIds?: string[];
}

export interface BulkInviteRequest {
  emails: string[];
  role: string;
  dashboardIds?: string[];
}

export interface BulkInviteResult {
  invited: number;
  alreadyPending: number;
  alreadyMember: number;
  invalid: number;
}

export interface InvitationCheck {
  invited: boolean;
  organizationName?: string;
  role?: string;
}

export interface VerifyEmailResponse {
  email: string;
  joinedOrganization: boolean;
  organizationId?: string;
  role?: string;
  dashboardId?: string;
}
