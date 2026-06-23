import { Routes } from "@angular/router";
import { billingRouteGuard } from "../../core/guards/subscription.guard";

export const settingsRoutes: Routes = [
  {
    path: "",
    loadComponent: () =>
      import("./components/settings-layout/settings-layout.component").then(
        (m) => m.SettingsLayoutComponent,
      ),
    children: [
      {
        path: "jira",
        loadComponent: () =>
          import("./components/jira-config/jira-config.component").then(
            (m) => m.JiraConfigComponent,
          ),
      },
      {
        path: "billing",
        canActivate: [billingRouteGuard],
        loadComponent: () =>
          import("./components/billing/billing.component").then(
            (m) => m.BillingComponent,
          ),
      },
      {
        path: "language",
        loadComponent: () =>
          import("./components/language-settings/language-settings.component").then(
            (m) => m.LanguageSettingsComponent,
          ),
      },
      {
        path: "members",
        loadComponent: () =>
          import("./components/members/members.component").then(
            (m) => m.MembersComponent,
          ),
      },
      {
        path: "",
        redirectTo: "jira",
        pathMatch: "full",
      },
    ],
  },
];
