import {
  Component,
  ElementRef,
  HostListener,
  inject,
  signal,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { LanguageSettingsApiService } from "../../../../core/services/language-settings-api.service";
import { AppLanguage } from "../../../../core/models/language-settings.model";
import { I18nService } from "../../../../i18n/i18n.service";
import { TranslatePipe } from "../../../../shared/pipes/translate.pipe";

@Component({
  selector: "app-language-settings",
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: "./language-settings.component.html",
  styleUrls: ["./language-settings.component.scss"],
})
export class LanguageSettingsComponent {
  private readonly api = inject(LanguageSettingsApiService);
  private readonly elementRef = inject(ElementRef<HTMLElement>);
  readonly i18n = inject(I18nService);

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly success = signal<string | null>(null);
  readonly error = signal<string | null>(null);
  readonly dropdownOpen = signal(false);

  language: AppLanguage = "fr";

  readonly languageOptions: Array<{ value: AppLanguage; label: string }> = [
    { value: "fr", label: "Francais" },
    { value: "en", label: "English" },
  ];

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);

    this.api.getLanguageSettings().subscribe({
      next: (response) => {
        // Only populate the dropdown — do NOT apply the language.
        // The active language must only change when the user explicitly saves.
        this.language = response.language ?? this.i18n.language();
        this.loading.set(false);
      },
      error: () => {
        // Fallback: mirror whatever is already active so the dropdown is coherent.
        const local = localStorage.getItem("app_language");
        if (local === "fr" || local === "en") {
          this.language = local;
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
        this.i18n.setLanguage(this.language);
        this.success.set("lang.saved");
        this.saving.set(false);
      },
      error: (err) => {
        this.error.set(err?.message ?? "lang.error");
        this.saving.set(false);
      },
    });
  }

  get selectedLanguageLabel(): string {
    return (
      this.languageOptions.find((option) => option.value === this.language)
        ?.label ?? "Francais"
    );
  }

  toggleDropdown(): void {
    if (this.loading() || this.saving()) {
      return;
    }

    this.dropdownOpen.update((open) => !open);
  }

  selectLanguage(language: AppLanguage): void {
    this.language = language;
    this.dropdownOpen.set(false);
  }

  @HostListener("document:click", ["$event"])
  onDocumentClick(event: MouseEvent): void {
    if (!this.dropdownOpen()) {
      return;
    }

    if (!this.elementRef.nativeElement.contains(event.target as Node)) {
      this.dropdownOpen.set(false);
    }
  }
}
