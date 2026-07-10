import { Injectable, inject, signal } from "@angular/core";
import { Observable, tap } from "rxjs";
import { JiraConfigApiService } from "./jira-config-api.service";
import { MyJiraCredentials } from "../models/jira-config.model";

@Injectable({ providedIn: "root" })
export class JiraCredentialsStateService {
  private readonly api = inject(JiraConfigApiService);

  private readonly _credentials = signal<MyJiraCredentials | null>(null);

  /** Read-only signal — null means not yet loaded */
  readonly credentials = this._credentials.asReadonly();

  /**
   * Loads the personal Jira credentials from the backend and caches them.
   * Returns the Observable so callers (guards) can wait for the result.
   */
  load(): Observable<MyJiraCredentials> {
    return this.api
      .getMyCredentials()
      .pipe(tap((creds) => this._credentials.set(creds)));
  }

  /** Called after a successful PUT /my-credentials */
  setConnected(username: string): void {
    this._credentials.set({ connected: true, username });
  }

  /** Reset on logout */
  clear(): void {
    this._credentials.set(null);
  }
}
