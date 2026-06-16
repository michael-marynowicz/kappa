import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import {
  JiraConfig,
  UpdateJiraConfigRequest,
  JiraTestResult,
} from "../models/jira-config.model";
import { environment } from "../../../environments/environment";

@Injectable({ providedIn: "root" })
export class JiraConfigApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/api/v1/jira`;

  getConfig(): Observable<JiraConfig> {
    return this.http.get<JiraConfig>(this.baseUrl);
  }

  updateConfig(request: UpdateJiraConfigRequest): Observable<JiraConfig> {
    return this.http.put<JiraConfig>(this.baseUrl, request);
  }

  testConnection(): Observable<JiraTestResult> {
    return this.http.post<JiraTestResult>(`${this.baseUrl}/test`, {});
  }

  sync(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/sync`, {});
  }
}
