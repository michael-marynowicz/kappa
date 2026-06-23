import { Component, inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterOutlet, RouterLink, RouterLinkActive } from "@angular/router";
import { AuthStateService } from "../../../../core/services/auth-state.service";
import { TranslatePipe } from "../../../../shared/pipes/translate.pipe";

@Component({
  selector: "app-settings-layout",
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    TranslatePipe,
  ],
  templateUrl: "./settings-layout.component.html",
  styleUrls: ["./settings-layout.component.scss"],
})
export class SettingsLayoutComponent {
  readonly authState = inject(AuthStateService);

  get isAdmin(): boolean {
    return this.authState.user()?.role === "ADMIN";
  }
}
