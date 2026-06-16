import { Component, inject, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { LanguageSettingsApiService } from "../../../../core/services/language-settings-api.service";
import { AppLanguage } from "../../../../core/models/language-settings.model";

@Component({
  selector: "app-language-settings",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./language-settings.component.html",
  styleUrls: ["./language-settings.component.scss"],
})
export class LanguageSettingsComponent {
  private readonly api = inject(LanguageSettingsApiService);

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly success = signal<string | null>(null);
  readonly error = signal<string | null>(null);

  language: AppLanguage = "fr";

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);

    this.api.getLanguageSettings().subscribe({
      next: (response) => {
        this.language = response.language ?? "fr";
        this.applyLanguage(this.language);
        this.loading.set(false);
      },
      error: () => {
        // Keep the app usable if backend has not implemented this endpoint yet.
        const local = localStorage.getItem("app_language");
        if (local === "fr" || local === "en") {
          this.language = local;
          this.applyLanguage(this.language);
        }
        this.loading.set(false);
      },
    });
  }

  onSave(): void {
    this.saving.set(true);
    this.error.set(null);
    this.success.set(null);

    this.api.updateLanguageSettings({ language: this.language }).subscribe({
      next: (response) => {
        this.language = response.language;
        this.applyLanguage(this.language);
        this.success.set("Language preference saved.");
        this.saving.set(false);
      },
      error: (err) => {
        this.error.set(err?.message ?? "Failed to save language preference.");
        this.saving.set(false);
      },
    });
  }

  private applyLanguage(language: AppLanguage): void {
    localStorage.setItem("app_language", language);
    document.documentElement.lang = language;
  }
}
