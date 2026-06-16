import {
  Directive,
  Input,
  ElementRef,
  Renderer2,
  inject,
  effect,
  OnDestroy,
} from "@angular/core";
import { Router } from "@angular/router";
import { PermissionService } from "../../core/services/permission.service";

/**
 * Attribute directive for disabling interactive elements (buttons, links)
 * when the user lacks a specific permission.
 *
 * Usage:
 *   <button appFeatureButton="EXPORT_CSV">Export CSV</button>
 *
 * When disabled:
 * - Adds `feature-gate--disabled` CSS class
 * - Sets `disabled` attribute
 * - Blocks click events
 * - Shows tooltip on hover
 */
@Directive({
  selector: "[appFeatureButton]",
  standalone: true,
})
export class FeatureButtonDirective implements OnDestroy {
  private readonly permissionService = inject(PermissionService);
  private readonly el = inject(ElementRef);
  private readonly renderer = inject(Renderer2);
  private readonly router = inject(Router);

  private effectRef: ReturnType<typeof effect> | null = null;
  private clickListener: (() => void) | null = null;
  private isDisabled = false;

  @Input()
  set appFeatureButton(permission: string) {
    this._permission = permission;
    this.setupEffect();
  }

  @Input() featureButtonTooltip = "Premium feature – Upgrade to unlock";

  private _permission = "";

  private setupEffect(): void {
    this.effectRef?.destroy();

    this.effectRef = effect(() => {
      const hasAccess = this.permissionService.hasPermissionSignal(
        this._permission,
      )();
      this.updateElement(!hasAccess);
    });
  }

  private updateElement(disabled: boolean): void {
    const el = this.el.nativeElement;

    if (disabled && !this.isDisabled) {
      this.renderer.addClass(el, "feature-gate--disabled");
      this.renderer.setAttribute(el, "disabled", "true");
      this.renderer.setAttribute(el, "title", this.featureButtonTooltip);
      this.clickListener = this.renderer.listen(el, "click", (event: Event) => {
        event.stopPropagation();
        event.preventDefault();
        this.router.navigate(["/settings/billing"]);
      });
      this.isDisabled = true;
    } else if (!disabled && this.isDisabled) {
      this.renderer.removeClass(el, "feature-gate--disabled");
      this.renderer.removeAttribute(el, "disabled");
      this.renderer.removeAttribute(el, "title");
      this.clickListener?.();
      this.clickListener = null;
      this.isDisabled = false;
    }
  }

  ngOnDestroy(): void {
    this.effectRef?.destroy();
    this.clickListener?.();
  }
}
