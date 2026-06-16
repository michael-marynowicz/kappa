export type MemberRole = "DEV" | "PDA" | "QA";

export interface TeamMember {
  id: string;
  name: string;
  role: MemberRole;
}

export interface CapacityGrid {
  members: TeamMember[];
  sprints: string[];
  daysPerSprint: Record<string, number>;
  daysOffGrid: Record<string, Record<string, number>>;
}

export interface CreateMemberRequest {
  name: string;
  role: MemberRole;
}

export interface UpdateMemberRequest {
  name: string;
  role: MemberRole;
}

export interface UpdateDaysOffRequest {
  teamMemberId: string;
  sprintName: string;
  daysOff: number;
}
