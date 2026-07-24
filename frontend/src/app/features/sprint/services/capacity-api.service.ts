import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import {
  CapacityGrid,
  TeamMember,
  CreateMemberRequest,
  UpdateMemberRequest,
  UpdateDaysOffRequest,
} from "../models/capacity.model";
import { environment } from "../../../../environments/environment";

@Injectable({ providedIn: "root" })
export class CapacityApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/api/v1/capacity`;

  getCapacityGrid(): Observable<CapacityGrid> {
    return this.http.get<CapacityGrid>(this.baseUrl);
  }

  addMember(request: CreateMemberRequest): Observable<TeamMember> {
    return this.http.post<TeamMember>(`${this.baseUrl}/members`, request);
  }

  updateMember(
    id: string,
    request: UpdateMemberRequest,
  ): Observable<TeamMember> {
    return this.http.put<TeamMember>(
      `${this.baseUrl}/members/${encodeURIComponent(id)}`,
      request,
    );
  }

  deleteMember(id: string): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/members/${encodeURIComponent(id)}`,
    );
  }

  updateDaysOff(request: UpdateDaysOffRequest): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/days-off`, request);
  }

  updateDaysOffBulk(requests: UpdateDaysOffRequest[]): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/days-off/bulk`, requests);
  }

  updateTimeOverride(memberId: string, timeOverride: number): Observable<void> {
    return this.http.put<void>(
      `${this.baseUrl}/members/${encodeURIComponent(memberId)}/time-override`,
      { timeOverride },
    );
  }

  exportXlsx(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/export`, {
      responseType: "blob",
    });
  }
}
