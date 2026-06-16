import { Routes } from "@angular/router";

export const capacityRoutes: Routes = [
  {
    path: "",
    loadComponent: () =>
      import("./capacity-page.component").then((m) => m.CapacityPageComponent),
  },
];
