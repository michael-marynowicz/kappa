export interface Invitation {
  id: string;
  email: string;
  role: string;
  status: "PENDING" | "ACCEPTED" | "EXPIRED";
  createdAt: string;
}

export interface CreateInvitationRequest {
  email: string;
  role: string;
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
}
