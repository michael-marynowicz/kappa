import { Pipe, PipeTransform, inject } from "@angular/core";
import { I18nService } from "../../i18n/i18n.service";

/**
 * Impure pipe so it re-evaluates on every CD cycle.
 * Since `I18nService.t()` reads a signal, language changes schedule a new CD
 * cycle (via zone.js integration), causing all pipe calls to re-run.
 *
 * Usage: {{ 'some.key' | translate }}
 */
@Pipe({ name: "translate", pure: false, standalone: true })
export class TranslatePipe implements PipeTransform {
  private readonly i18n = inject(I18nService);

  transform(key: string): string {
    return this.i18n.t(key);
  }
}
