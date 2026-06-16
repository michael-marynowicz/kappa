import { Component, Input } from "@angular/core";
import { CommonModule } from "@angular/common";

/**
 * Reusable status badge: renders a colored pill for Jira issue statuses.
 * Purely presentational — no logic beyond CSS class mapping.
 */
@Component({
  selector: "app-status-badge",
  standalone: true,
  imports: [CommonModule],
  templateUrl: "./status-badge.component.html",
  styles: [
    `
      .badge {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 5px 12px;
        border-radius: 99px;
        font-size: 11px;
        font-weight: 600;
        letter-spacing: 0.2px;
        white-space: nowrap;
        border: 1px solid transparent;
        transition:
          transform 150ms ease,
          box-shadow 150ms ease;
      }

      .badge__dot {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        flex-shrink: 0;
      }

      .badge--done {
        background: rgba(16, 185, 129, 0.1);
        color: #34d399;
        border-color: rgba(16, 185, 129, 0.2);

        .badge__dot {
          background: #10b981;
          box-shadow: 0 0 6px rgba(16, 185, 129, 0.5);
        }
      }

      .badge--in-progress {
        background: rgba(59, 130, 246, 0.1);
        color: #60a5fa;
        border-color: rgba(59, 130, 246, 0.2);

        .badge__dot {
          background: #3b82f6;
          box-shadow: 0 0 6px rgba(59, 130, 246, 0.5);
          animation: pulse 2s ease-in-out infinite;
        }
      }

      .badge--in-review {
        background: rgba(139, 92, 246, 0.1);
        color: #a78bfa;
        border-color: rgba(139, 92, 246, 0.2);

        .badge__dot {
          background: #8b5cf6;
          box-shadow: 0 0 6px rgba(139, 92, 246, 0.5);
        }
      }

      .badge--to-do {
        background: rgba(100, 116, 139, 0.1);
        color: #94a3b8;
        border-color: rgba(100, 116, 139, 0.2);

        .badge__dot {
          background: #64748b;
        }
      }

      .badge--completed {
        background: rgba(16, 185, 129, 0.1);
        color: #34d399;
        border-color: rgba(16, 185, 129, 0.2);

        .badge__dot {
          background: #10b981;
          box-shadow: 0 0 6px rgba(16, 185, 129, 0.5);
        }
      }

      .badge--default {
        background: rgba(100, 116, 139, 0.08);
        color: #94a3b8;
        border-color: rgba(100, 116, 139, 0.15);

        .badge__dot {
          background: #64748b;
        }
      }

      @keyframes pulse {
        0%,
        100% {
          opacity: 1;
          transform: scale(1);
        }
        50% {
          opacity: 0.6;
          transform: scale(0.85);
        }
      }
    `,
  ],
})
export class StatusBadgeComponent {
  @Input({ required: true }) status!: string;

  get badgeClass(): string {
    const normalized = this.status?.toLowerCase().replace(/\s+/g, "-") ?? "";
    const knownStatuses = [
      "done",
      "in-progress",
      "in-review",
      "to-do",
      "completed",
    ];
    return knownStatuses.includes(normalized)
      ? `badge--${normalized}`
      : "badge--default";
  }
}
