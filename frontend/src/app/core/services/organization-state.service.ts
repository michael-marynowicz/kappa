import { Injectable, inject, signal } from "@angular/core";
import { OrganizationApiService } from "./organization-api.service";
import { Organization } from "../models/organization.model";

@Injectable({ providedIn: "root" })
export class OrganizationStateService {
  private readonly api = inject(OrganizationApiService);

  private readonly _organization = signal<Organization | null>(null);
  private readonly _loading = signal(false);
  private readonly _error = signal<string | null>(null);

  readonly organization = this._organization.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();

  loadOrganization(): void {
    this._loading.set(true);
    this._error.set(null);
    this.api.getCurrent().subscribe({
      next: (org) => {
        this._organization.set(org);
        this._loading.set(false);
      },
      error: (err) => {
        this._error.set(err.message ?? "Failed to load organization");
        this._loading.set(false);
      },
    });
  }

  clear(): void {
    this._organization.set(null);
  }
}
