import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import {
  SprintIssue,
  SprintMetrics,
  UpdateRemainingSpRequest,
  IterationSnapshot,
  EpicGroup,
} from "../models/sprint-issue.model";
import { environment } from "../../../../environments/environment";

/**
 * Service responsible for all HTTP communication with the Kappa API.
 * Components NEVER call HttpClient directly.
 */
@Injectable({ providedIn: "root" })
export class SprintApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/api/v1`;

  getSprintIssues(): Observable<SprintIssue[]> {
    return this.http.get<SprintIssue[]>(`${this.baseUrl}/issues`);
  }

  getGroupedIssues(): Observable<EpicGroup[]> {
    return this.http.get<EpicGroup[]>(`${this.baseUrl}/issues/grouped`);
  }

  getMetrics(): Observable<SprintMetrics> {
    return this.http.get<SprintMetrics>(`${this.baseUrl}/metrics`);
  }

  getIterationHistory(): Observable<IterationSnapshot[]> {
    return this.http.get<IterationSnapshot[]>(
      `${this.baseUrl}/metrics/iterations`,
    );
  }

  updateRemainingStoryPoints(
    request: UpdateRemainingSpRequest,
  ): Observable<SprintIssue> {
    return this.http.post<SprintIssue>(
      `${this.baseUrl}/issues/update`,
      request,
    );
  }

  exportCsv(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/export/csv`, {
      responseType: "blob",
    });
  }
}
