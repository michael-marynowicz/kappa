import { Injectable, signal } from "@angular/core";
import { AppLanguage } from "../core/models/language-settings.model";
import { TRANSLATIONS } from "./translations";

/**
 * Application-level i18n service.
 *
 * - Bootstraps from localStorage (set by LanguageSettingsComponent on save).
 * - Falls back to browser language, then "en".
 * - Exposes a reactive `language` signal so consumers can react to changes.
 * - `t(key)` reads the signal, making template/pipe calls auto-reactive.
 */
@Injectable({ providedIn: "root" })
export class I18nService {
  private readonly _language = signal<AppLanguage>(
    this.resolveInitialLanguage(),
  );

  readonly language = this._language.asReadonly();

  /** Change the active language and persist the preference. */
  setLanguage(lang: AppLanguage): void {
    this._language.set(lang);
    localStorage.setItem("app_language", lang);
    document.documentElement.lang = lang;
  }

  /**
   * Translate a key.
   * Falls back to the EN dictionary, then returns the raw key.
   * Reading `_language()` here registers this as a signal dependency.
   */
  t(key: string): string {
    const lang = this._language();
    return TRANSLATIONS[lang][key] ?? TRANSLATIONS["en"][key] ?? key;
  }

  /**
   * Translate a key and substitute `{param}` placeholders.
   * e.g. tWithParams('jira.success.dashboard_created', { name: 'My Board' })
   */
  tWithParams(key: string, params: Record<string, string>): string {
    let text = this.t(key);
    for (const [k, v] of Object.entries(params)) {
      text = text.replace(`{${k}}`, v);
    }
    return text;
  }

  private resolveInitialLanguage(): AppLanguage {
    const stored = localStorage.getItem("app_language");
    if (stored === "fr" || stored === "en") return stored;
    const browser = navigator.language.slice(0, 2);
    return browser === "fr" ? "fr" : "en";
  }
}
