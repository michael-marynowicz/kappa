import { Component, inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import { CapacityGridComponent } from "../sprint/components/capacity-grid/capacity-grid.component";
import { CapacityStateService } from "../sprint/services/capacity-state.service";

@Component({
  selector: "app-capacity-page",
  standalone: true,
  imports: [CommonModule, CapacityGridComponent],
  templateUrl: "./capacity-page.component.html",
  styles: [
    `
      .capacity-page {
        max-width: 1200px;
      }
      .capacity__header {
        display: flex;
        align-items: flex-start;
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
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 8px 16px;
        background: rgba(255, 255, 255, 0.04);
        border: 1px solid rgba(255, 255, 255, 0.1);
        border-radius: 8px;
        color: #e4e7ef;
        font-size: 13px;
        font-weight: 500;
        cursor: pointer;
        transition: background 150ms ease;
      }
      .btn-export:hover {
        background: rgba(255, 255, 255, 0.08);
      }
    `,
  ],
})
export class CapacityPageComponent {
  readonly capState = inject(CapacityStateService);

  onExportCsv(): void {
    this.capState.exportCsv();
  }
}
