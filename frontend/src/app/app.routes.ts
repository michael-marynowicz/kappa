import { Routes } from "@angular/router";
import { authGuard, guestGuard } from "./core/guards/auth.guard";
import { adminGuard } from "./core/guards/admin.guard";
import { subscriptionGuard } from "./core/guards/subscription.guard";

export const routes: Routes = [
  // Public landing page
  {
    path: "home",
    loadComponent: () =>
      import("./features/home/home.component").then((m) => m.HomeComponent),
  },
  // Public auth routes
  {
    path: "auth",
    canActivate: [guestGuard],
    loadChildren: () =>
      import("./features/auth/auth.routes").then((m) => m.authRoutes),
  },
  // Verify email - accessible regardless of auth state
  {
    path: "verify-email",
    loadComponent: () =>
      import("./features/auth/components/verify-email/verify-email.component").then(
        (m) => m.VerifyEmailComponent,
      ),
  },
  // Protected routes with shell layout
  {
    path: "",
    canActivate: [authGuard],
    loadComponent: () =>
      import("./layout/shell/shell.component").then((m) => m.ShellComponent),
    children: [
      {
        path: "",
        redirectTo: "sprint",
        pathMatch: "full",
      },
      {
        path: "sprint",
        canActivate: [subscriptionGuard],
        loadChildren: () =>
          import("./features/sprint/sprint.routes").then((m) => m.sprintRoutes),
      },
      {
        path: "backlog",
        canActivate: [subscriptionGuard],
        loadChildren: () =>
          import("./features/backlog/backlog.routes").then(
            (m) => m.backlogRoutes,
          ),
      },
      {
        path: "metrics",
        canActivate: [subscriptionGuard],
        loadChildren: () =>
          import("./features/metrics/metrics.routes").then(
            (m) => m.metricsRoutes,
          ),
      },
      {
        path: "capacity",
        canActivate: [subscriptionGuard],
        loadChildren: () =>
          import("./features/capacity/capacity.routes").then(
            (m) => m.capacityRoutes,
          ),
      },
      {
        path: "settings",
        canActivate: [adminGuard],
        loadChildren: () =>
          import("./features/settings/settings.routes").then(
            (m) => m.settingsRoutes,
          ),
      },
    ],
  },
  {
    path: "**",
    redirectTo: "home",
  },
];
