import { Injectable, inject, signal } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { environment } from "../../../../environments/environment";

@Injectable({ providedIn: "root" })
export class CurrentIterationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/api/v1/capacity/current-iteration`;
  readonly name = signal<string | null>(null);

  fetch(): void {
    this.http.get<{ name: string }>(this.baseUrl).subscribe({
      next: (data) => this.name.set(data.name),
      error: () => this.name.set(null),
    });
  }
}
