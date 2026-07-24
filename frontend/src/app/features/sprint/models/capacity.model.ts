export type MemberRole = "DEV" | "PDA" | "QA" | "SM";

export interface TeamMember {
  id: string;
  name: string;
  role: MemberRole;
  timeOverride: number; // 0-1
}

export interface SprintDetails {
  pi: string; // e.g. "26.3"
  iteration: number;
  ip: boolean; // true = IP week, false = IT week
}

export interface CapacityGrid {
  members: TeamMember[];
  sprints: string[];
  daysPerSprint: Record<string, number>;
  daysOffGrid: Record<string, Record<string, number>>;
  sprintDetails: Record<string, SprintDetails>;
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
