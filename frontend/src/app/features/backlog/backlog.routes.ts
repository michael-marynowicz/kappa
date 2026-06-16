import { Routes } from "@angular/router";

export const backlogRoutes: Routes = [
  {
    path: "",
    loadComponent: () =>
      import("./backlog.component").then((m) => m.BacklogComponent),
  },
];
