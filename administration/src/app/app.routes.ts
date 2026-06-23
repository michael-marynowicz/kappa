import { Routes } from "@angular/router";

export const routes: Routes = [
  {
    path: "login",
    loadComponent: () =>
      import("./login/login.component").then((m) => m.LoginComponent),
  },
  {
    path: "dashboard",
    loadComponent: () =>
      import("./dashboard/dashboard.component").then(
        (m) => m.DashboardComponent,
      ),
  },
  {
    path: "org/:id",
    loadComponent: () =>
      import("./org-detail/org-detail.component").then(
        (m) => m.OrgDetailComponent,
      ),
  },
  { path: "", redirectTo: "dashboard", pathMatch: "full" },
  { path: "**", redirectTo: "dashboard" },
];
