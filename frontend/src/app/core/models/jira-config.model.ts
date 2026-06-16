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

export interface UpdateJiraConfigRequest {
  baseUrl: string;
  authType: string;
  userEmail: string;
  projectKey: string;
  boardId: number;
  token: string;
}

export interface JiraTestResult {
  success: boolean;
  message: string;
}
