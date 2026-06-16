import { Component, inject, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { SprintStateService } from "../sprint/services/sprint-state.service";
import { SprintIssueTableComponent } from "../sprint/components/sprint-issue-table/sprint-issue-table.component";
import { SprintSummaryCardComponent } from "../sprint/components/sprint-summary-card/sprint-summary-card.component";

@Component({
  selector: "app-backlog",
  standalone: true,
  imports: [
    CommonModule,
    SprintIssueTableComponent,
    SprintSummaryCardComponent,
  ],
  templateUrl: "./backlog.component.html",
  styles: [
    `
      .backlog {
        max-width: 1200px;
      }
      .backlog__header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 24px;
      }
      .page-title {
        font-size: 22px;
        font-weight: 700;
        color: #e4e7ef;
        margin: 0 0 4px;
      }
      .page-subtitle {
        font-size: 13px;
        color: #8b92a8;
        margin: 0;
      }
      .btn-export {
        padding: 8px 16px;
        background: rgba(255, 255, 255, 0.04);
        border: 1px solid rgba(255, 255, 255, 0.1);
        border-radius: 8px;
        color: #e4e7ef;
        font-size: 13px;
        cursor: pointer;
        &:hover {
          background: rgba(255, 255, 255, 0.08);
        }
      }
      .error-banner {
        display: flex;
        align-items: center;
        justify-content: space-between;
        background: rgba(248, 113, 113, 0.08);
        border: 1px solid rgba(248, 113, 113, 0.2);
        border-radius: 8px;
        padding: 10px 14px;
        font-size: 12px;
        color: #f87171;
        margin-bottom: 16px;
        button {
          background: none;
          border: none;
          color: #f87171;
          cursor: pointer;
        }
      }
      .loading-state {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 12px;
        padding: 60px 0;
        color: #8b92a8;
      }
      .loading-spinner {
        width: 24px;
        height: 24px;
        border: 2px solid rgba(255, 255, 255, 0.1);
        border-top-color: #818cf8;
        border-radius: 50%;
        animation: spin 0.6s linear infinite;
      }
      @keyframes spin {
        to {
          transform: rotate(360deg);
        }
      }
    `,
  ],
})
export class BacklogComponent implements OnInit {
  readonly state = inject(SprintStateService);

  ngOnInit(): void {
    this.state.loadIssues();
  }

  onUpdateSp(event: { issueKey: string; remainingStoryPoints: number }): void {
    this.state.updateRemainingStoryPoints(
      event.issueKey,
      event.remainingStoryPoints,
    );
  }

  onExportCsv(): void {
    this.state.exportCsv();
  }
}
