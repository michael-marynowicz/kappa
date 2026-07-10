import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import {
  JiraConfig,
  UpdateJiraCredentialsRequest,
  JiraTestResult,
  JiraBoardDiscoveryRequest,
  JiraDiscoveredBoard,
  CreateJiraDashboardRequest,
  JiraDashboard,
  MyJiraCredentials,
  SaveMyJiraCredentialsRequest,
} from "../models/jira-config.model";
import { environment } from "../../../environments/environment";

@Injectable({ providedIn: "root" })
export class JiraConfigApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/api/v1/jira`;

  startOAuthConnect(): Observable<{ authUrl: string }> {
    return this.http.get<{ authUrl: string }>(`${this.baseUrl}/oauth/connect`);
  }

  disconnectOAuth(): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/oauth/disconnect`);
  }

  discoverBoards(
    request: JiraBoardDiscoveryRequest,
  ): Observable<JiraDiscoveredBoard[]> {
    return this.http.post<JiraDiscoveredBoard[]>(
      `${this.baseUrl}/boards/discover`,
      request,
    );
  }

  createDashboard(
    request: CreateJiraDashboardRequest,
  ): Observable<JiraDashboard> {
    return this.http.post<JiraDashboard>(`${this.baseUrl}/dashboards`, request);
  }

  listDashboards(): Observable<JiraDashboard[]> {
    return this.http.get<JiraDashboard[]>(`${this.baseUrl}/dashboards`);
  }

  activateDashboard(id: string): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/dashboards/${id}/activate`, {});
  }

  deleteDashboard(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/dashboards/${id}`);
  }

  getConfig(): Observable<JiraConfig> {
    return this.http.get<JiraConfig>(this.baseUrl);
  }

  updateCredentials(
    request: UpdateJiraCredentialsRequest,
  ): Observable<JiraConfig> {
    return this.http.put<JiraConfig>(`${this.baseUrl}/credentials`, request);
  }

  testConnection(): Observable<JiraTestResult> {
    return this.http.post<JiraTestResult>(`${this.baseUrl}/test`, {});
  }

  sync(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/sync`, {});
  }

  /** GET /api/v1/jira/my-credentials — personal connection status for the current user */
  getMyCredentials(): Observable<MyJiraCredentials> {
    return this.http.get<MyJiraCredentials>(`${this.baseUrl}/my-credentials`);
  }

  /** PUT /api/v1/jira/my-credentials — save personal Jira credentials */
  saveMyCredentials(
    request: SaveMyJiraCredentialsRequest,
  ): Observable<MyJiraCredentials> {
    return this.http.put<MyJiraCredentials>(
      `${this.baseUrl}/my-credentials`,
      request,
    );
  }
}
