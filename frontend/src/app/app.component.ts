import { Component, inject, OnInit } from "@angular/core";
import { RouterOutlet } from "@angular/router";
import { AuthStateService } from "./core/services/auth-state.service";
import { OrganizationStateService } from "./core/services/organization-state.service";

@Component({
  selector: "app-root",
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: "./app.component.html",
})
export class AppComponent implements OnInit {
  private readonly authState = inject(AuthStateService);
  private readonly orgState = inject(OrganizationStateService);

  ngOnInit(): void {
    const savedLanguage = localStorage.getItem("app_language");
    if (savedLanguage === "fr" || savedLanguage === "en") {
      globalThis.document.documentElement.lang = savedLanguage;
    }

    if (this.authState.getToken()) {
      this.authState.loadCurrentUser();
      if (!this.orgState.organization()) this.orgState.loadOrganization();
    }
  }
}
