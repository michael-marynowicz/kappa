import { Component, Input, inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Router } from "@angular/router";

@Component({
  selector: "app-premium-overlay",
  standalone: true,
  imports: [CommonModule],
  templateUrl: "./premium-overlay.component.html",
  styles: [
    `
      .premium-overlay {
        position: absolute;
        inset: 0;
        z-index: 100;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        border-radius: inherit;
      }

      .premium-overlay__backdrop {
        position: absolute;
        inset: 0;
        background: rgba(15, 23, 42, 0.6);
        backdrop-filter: blur(2px);
        border-radius: inherit;
      }

      .premium-overlay__watermark {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%) rotate(-15deg);
        font-size: 4rem;
        font-weight: 800;
        text-transform: uppercase;
        color: rgba(255, 255, 255, 0.06);
        letter-spacing: 0.2em;
        pointer-events: none;
        white-space: nowrap;
      }

      .premium-overlay__content {
        position: relative;
        z-index: 1;
        text-align: center;
        padding: 1.5rem;
      }

      .premium-overlay__icon {
        width: 2.5rem;
        height: 2.5rem;
        color: #f59e0b;
        margin-bottom: 0.75rem;
      }

      .premium-overlay__card {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 0.75rem;
      }

      .premium-overlay__message {
        color: #e2e8f0;
        font-size: 0.875rem;
        font-weight: 500;
        margin: 0;
        max-width: 240px;
      }

      .premium-overlay__cta {
        background: linear-gradient(135deg, #f59e0b, #d97706);
        color: #fff;
        border: none;
        border-radius: 6px;
        padding: 0.5rem 1.25rem;
        font-size: 0.8rem;
        font-weight: 600;
        cursor: pointer;
        transition:
          transform 0.15s ease,
          box-shadow 0.15s ease;
        box-shadow: 0 2px 8px rgba(245, 158, 11, 0.3);
      }

      .premium-overlay__cta:hover {
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(245, 158, 11, 0.4);
      }

      .premium-overlay__cta:active {
        transform: translateY(0);
      }
    `,
  ],
})
export class PremiumOverlayComponent {
  private readonly router = inject(Router);

  @Input() message = "";

  onUpgradeClick(event: Event): void {
    event.stopPropagation();
    event.preventDefault();
    this.router.navigate(["/settings/billing"]);
  }
}
