/**
 * Domain model: a sprint issue with all KPI fields.
 */
export interface SprintIssue {
  issueKey: string;
  summary: string;
  status: string;
  assignee: string | null;
  issueType: string;
  totalStoryPoints: number | null;
  remainingStoryPoints: number | null;
  doneStoryPoints: number | null;
}

export interface UpdateRemainingSpRequest {
  issueKey: string;
  remainingStoryPoints: number;
}

/** Frontend-computed sprint summary (used in KPI cards). */
export interface SprintSummary {
  totalIssues: number;
  totalStoryPoints: number;
  remainingStoryPoints: number;
  doneStoryPoints: number;
  completionPercentage: number;
}

/** Full SM metrics returned by GET /api/v1/metrics */
export interface SprintMetrics {
  committedStoryPoints: number;
  deliveredStoryPoints: number;
  workStoryPoints: number;
  leftoverStoryPoints: number;
  remainingStoryPoints: number;
  ratio: number | null;
  velocity: number;
  predictabilityRate: number | null;
  sprintSuccess: boolean | null;
  totalIssues: number;
  completedIssues: number;
  inProgressIssues: number;
  todoIssues: number;
  blockedIssues: number;
  bugCount: number;
  storyCount: number;
  taskCount: number;
  blockedRatio: number | null;
  bugRatio: number | null;
  sprintFocusFactor: number | null;
  teamEfficiency: number | null;
  averageSpPerCompletedIssue: number | null;
  sprintHealthScore: number | null;
  throughput: number;
  workInProgress: number;
  carryOverIssues: number;
  /** Total issue count in sprint (committed + mid-sprint additions) */
  issueCount?: number;
  /** Story points grouped by topic/epic */
  topicBreakdown: TopicStoryPoints[];
  /** Real capacity in EFT days */
  realCapacity: number;
  /** Team availability in EFT */
  teamAvailability: TeamAvailability;
}

export interface TopicStoryPoints {
  topic: string;
  storyPoints: number;
  /** Issue count for this topic */
  issueCount?: number;
}

export interface TeamAvailability {
  dev: number;
  pda: number;
  qa: number;
}

/** Sprint snapshot for cross-iteration comparison */
export interface IterationSnapshot {
  sprintName: string;
  committedStoryPoints: number;
  deliveredStoryPoints: number;
  velocity: number;
  ratio: number | null;
}

/** Issue inside a grouped epic (from GET /api/v1/issues/grouped) */
export interface GroupedIssue {
  issueKey: string;
  summary: string;
  status: string;
  totalStoryPoints: number;
  doneStoryPoints: number;
}

/** Epic-level group returned by GET /api/v1/issues/grouped */
export interface EpicGroup {
  epicName: string;
  issueCount: number;
  totalStoryPoints: number;
  doneStoryPoints: number;
  remainingStoryPoints: number;
  completionPercentage: number;
  issues: GroupedIssue[];
}

export type IssueStatus =
  | "Done"
  | "In Progress"
  | "In Review"
  | "To Do"
  | string;
