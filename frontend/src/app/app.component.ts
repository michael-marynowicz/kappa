import { Component, inject, OnInit } from "@angular/core";
import { RouterOutlet } from "@angular/router";
import { AuthStateService } from "./core/services/auth-state.service";
import { OrganizationStateService } from "./core/services/organization-state.service";
import { SubscriptionStateService } from "./core/services/subscription-state.service";

@Component({
  selector: "app-root",
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: "./app.component.html",
})
export class AppComponent implements OnInit {
  private readonly authState = inject(AuthStateService);
  private readonly orgState = inject(OrganizationStateService);
  private readonly subState = inject(SubscriptionStateService);

  ngOnInit(): void {
    const savedLanguage = localStorage.getItem("app_language");
    if (savedLanguage === "fr" || savedLanguage === "en") {
      globalThis.document.documentElement.lang = savedLanguage;
    }

    if (this.authState.getToken()) {
      this.authState.loadCurrentUser();
      this.orgState.loadOrganization();
      this.subState.loadSubscription();
      this.subState.loadFeatures();
    }
  }
}
