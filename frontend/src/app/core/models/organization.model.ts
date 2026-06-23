export interface Organization {
  id: string;
  name: string;
  slug: string;
  createdAt: string;
}

export interface OrganizationMember {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  dashboardIds: string[];
}

export interface InviteMemberRequest {
  email: string;
  role: string;
}
