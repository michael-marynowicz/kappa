import {
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  Output,
  signal,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { JiraDashboard } from "../../../core/models/jira-config.model";

@Component({
  selector: "app-team-dashboard-switcher",
  standalone: true,
  imports: [CommonModule],
  templateUrl: "./team-dashboard-switcher.component.html",
  styleUrls: ["./team-dashboard-switcher.component.scss"],
})
export class TeamDashboardSwitcherComponent {
  @Input() dashboards: JiraDashboard[] = [];
  @Input() loading = false;
  @Input() switchingDashboardId: string | null = null;
  @Input() isAdmin = false;
  @Input() hintMessage = "Only admins can change team.";
  @Input() errorMessage: string | null = null;

  @Output() switchDashboard = new EventEmitter<string>();

  readonly dropdownOpen = signal(false);

  constructor(private readonly elementRef: ElementRef) {}

  @HostListener("document:click", ["$event"])
  onDocumentClick(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.dropdownOpen.set(false);
    }
  }

  get activeDashboard(): JiraDashboard | null {
    return this.dashboards.find((d) => d.active) ?? null;
  }

  toggleDropdown(): void {
    if (!this.isAdmin || this.switchingDashboardId !== null) {
      return;
    }
    this.dropdownOpen.update((v) => !v);
  }

  selectDashboard(dashboardId: string): void {
    this.dropdownOpen.set(false);
    this.switchDashboard.emit(dashboardId);
  }
}
