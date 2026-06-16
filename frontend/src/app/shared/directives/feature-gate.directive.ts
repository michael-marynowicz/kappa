import {
  Directive,
  Input,
  TemplateRef,
  ViewContainerRef,
  inject,
  effect,
  OnDestroy,
  ComponentRef,
} from "@angular/core";
import { PermissionService } from "../../core/services/permission.service";
import { FeatureGateState } from "../../core/models/permission.model";
import { PremiumOverlayComponent } from "../components/premium-overlay/premium-overlay.component";

@Directive({
  selector: "[appFeatureGate]",
  standalone: true,
})
export class FeatureGateDirective implements OnDestroy {
  private readonly permissionService = inject(PermissionService);
  private readonly templateRef = inject(TemplateRef<any>);
  private readonly viewContainer = inject(ViewContainerRef);

  private overlayRef: ComponentRef<PremiumOverlayComponent> | null = null;
  private hasView = false;
  private currentState: FeatureGateState | null = null;
  private effectRef: ReturnType<typeof effect> | null = null;

  /**
   * The permission code to check.
   * Usage: *appFeatureGate="'EXPORT_CSV'"
   */
  @Input()
  set appFeatureGate(permission: string) {
    this._permission = permission;
    this.setupEffect();
  }

  /**
   * If true, hides the element entirely when denied (instead of showing disabled with overlay).
   * Usage: *appFeatureGate="'EXPORT_CSV'; hide: true"
   */
  @Input() appFeatureGateHide = false;

  /**
   * Custom message for the premium overlay.
   */
  @Input() appFeatureGateMessage = "";

  private _permission = "";

  private setupEffect(): void {
    if (this.effectRef) {
      this.effectRef.destroy();
    }

    this.effectRef = effect(() => {
      const state = this.permissionService.featureState(
        this._permission,
        this.appFeatureGateHide,
      )();
      this.updateView(state);
    });
  }

  private updateView(state: FeatureGateState): void {
    if (state === this.currentState) return;
    this.currentState = state;

    switch (state) {
      case "enabled":
        this.showEnabled();
        break;
      case "disabled":
        this.showDisabled();
        break;
      case "hidden":
        this.hide();
        break;
    }
  }

  private showEnabled(): void {
    this.clearOverlay();
    if (!this.hasView) {
      this.viewContainer.clear();
      this.viewContainer.createEmbeddedView(this.templateRef);
      this.hasView = true;
    }
  }

  private showDisabled(): void {
    if (!this.hasView) {
      this.viewContainer.clear();
      this.viewContainer.createEmbeddedView(this.templateRef);
      this.hasView = true;
    }
    this.showOverlay();
  }

  private hide(): void {
    this.clearOverlay();
    this.viewContainer.clear();
    this.hasView = false;
  }

  private showOverlay(): void {
    if (this.overlayRef) return;
    this.overlayRef = this.viewContainer.createComponent(
      PremiumOverlayComponent,
    );
    if (this.appFeatureGateMessage) {
      this.overlayRef.instance.message = this.appFeatureGateMessage;
    }
  }

  private clearOverlay(): void {
    if (this.overlayRef) {
      this.overlayRef.destroy();
      this.overlayRef = null;
    }
  }

  ngOnDestroy(): void {
    this.effectRef?.destroy();
    this.clearOverlay();
  }
}
