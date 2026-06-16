import { Routes } from "@angular/router";

export const metricsRoutes: Routes = [
  {
    path: "",
    loadComponent: () =>
      import("./metrics.component").then((m) => m.MetricsComponent),
  },
];
