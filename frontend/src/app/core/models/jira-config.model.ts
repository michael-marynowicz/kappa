export interface JiraConfig {
  id: string;
  baseUrl: string;
  authType: string;
  userEmail: string;
  projectKey: string;
  boardId: number;
  token: string;
  connected: boolean;
  lastSyncAt: string | null;
}

export interface UpdateJiraCredentialsRequest {
  baseUrl: string;
  authType: string;
  userEmail: string;
  token: string;
}

export interface JiraTestResult {
  success: boolean;
  message: string;
}

export type JiraAuthType = "PAT" | "BASIC";

export interface JiraBoardDiscoveryRequest {
  baseUrl: string;
  authType: JiraAuthType;
  userEmail?: string;
  token: string;
}

export interface JiraDiscoveredBoard {
  id: number;
  name: string;
  projectKey: string;
  projectName: string;
  type: string;
}

export interface CreateJiraDashboardRequest {
  name: string;
  boardId: number;
  projectKey: string;
}

export interface JiraDashboard {
  id: string;
  name: string;
  boardId: number;
  projectKey: string;
  active: boolean;
  position: number;
}
