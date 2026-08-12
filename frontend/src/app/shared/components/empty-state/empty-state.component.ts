import { Component, input } from "@angular/core";
import { RouterLink } from "@angular/router";

@Component({
  selector: "app-empty-state",
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="empty-state">
      <div class="empty-state__icon" aria-hidden="true">
        <svg
          width="26"
          height="26"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="1.75"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <rect x="3" y="3" width="7" height="7" rx="1.5" />
          <rect x="14" y="3" width="7" height="7" rx="1.5" />
          <rect x="3" y="14" width="7" height="7" rx="1.5" />
          <rect x="14" y="14" width="7" height="7" rx="1.5" />
        </svg>
      </div>
      <p class="empty-state__title">{{ title() }}</p>
      <p class="empty-state__message">{{ message() }}</p>
      @if (actionLabel() && actionLink()) {
        <a class="empty-state__btn" [routerLink]="actionLink()">
          {{ actionLabel() }}
          <svg
            width="14"
            height="14"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2.5"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <line x1="5" y1="12" x2="19" y2="12" />
            <polyline points="12 5 19 12 12 19" />
          </svg>
        </a>
      }
    </div>
  `,
  styles: [
    `
      :host {
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 48px 24px;
        min-height: 320px;
      }
      .empty-state {
        display: flex;
        flex-direction: column;
        align-items: center;
        text-align: center;
        max-width: 380px;
        width: 100%;
        padding: 44px 32px;
        border: 1px dashed rgba(255, 255, 255, 0.12);
        border-radius: 16px;
        background: rgba(255, 255, 255, 0.02);
        animation: empty-state-in 300ms ease;
      }
      .empty-state__icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 64px;
        height: 64px;
        margin-bottom: 20px;
        border-radius: 50%;
        background: linear-gradient(
          135deg,
          rgba(99, 102, 241, 0.16),
          rgba(99, 102, 241, 0.04)
        );
        border: 1px solid rgba(99, 102, 241, 0.25);
        color: #818cf8;
      }
      .empty-state__title {
        font-size: 17px;
        font-weight: 700;
        color: #f1f3f9;
        margin: 0 0 8px;
        letter-spacing: -0.2px;
      }
      .empty-state__message {
        font-size: 13.5px;
        color: #8b92a8;
        margin: 0 0 26px;
        line-height: 1.6;
      }
      .empty-state__btn {
        display: inline-flex;
        align-items: center;
        gap: 7px;
        padding: 10px 20px;
        font-size: 13px;
        font-weight: 600;
        color: #fff;
        background: linear-gradient(135deg, #6366f1, #4f46e5);
        border: none;
        border-radius: 10px;
        text-decoration: none;
        box-shadow: 0 4px 14px rgba(99, 102, 241, 0.35);
        transition:
          transform 0.15s ease,
          box-shadow 0.15s ease;

        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 6px 18px rgba(99, 102, 241, 0.45);
        }
        &:active {
          transform: translateY(0);
        }
      }

      @keyframes empty-state-in {
        from {
          opacity: 0;
          transform: translateY(4px);
        }
        to {
          opacity: 1;
          transform: translateY(0);
        }
      }
    `,
  ],
})
export class EmptyStateComponent {
  readonly title = input.required<string>();
  readonly message = input.required<string>();
  readonly actionLabel = input<string>();
  readonly actionLink = input<string>();
}
