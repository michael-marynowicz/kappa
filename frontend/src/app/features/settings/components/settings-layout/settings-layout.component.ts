import { Component, inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterOutlet, RouterLink, RouterLinkActive } from "@angular/router";
import { AuthStateService } from "../../../../core/services/auth-state.service";
import { SubscriptionStateService } from "../../../../core/services/subscription-state.service";
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
  readonly subState = inject(SubscriptionStateService);

  get isAdmin(): boolean {
    return this.authState.user()?.role === "ADMIN";
  }

  // Billing tab: admins only, and only if the plan allows payment pages
  // subState.showPaymentPages() defaults true until subscription loads
  get showBillingTab(): boolean {
    return this.isAdmin && this.subState.showPaymentPages();
  }
}
