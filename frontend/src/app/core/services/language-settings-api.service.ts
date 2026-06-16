import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../../environments/environment";
import { LanguageSettings } from "../models/language-settings.model";

@Injectable({ providedIn: "root" })
export class LanguageSettingsApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/api/v1/settings/language`;

  getLanguageSettings(): Observable<LanguageSettings> {
    return this.http.get<LanguageSettings>(this.baseUrl);
  }

  updateLanguageSettings(request: LanguageSettings): Observable<LanguageSettings> {
    return this.http.put<LanguageSettings>(this.baseUrl, request);
  }
}
