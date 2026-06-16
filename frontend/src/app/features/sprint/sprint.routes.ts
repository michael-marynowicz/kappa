import { Routes } from '@angular/router';

export const sprintRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./components/sprint-dashboard/sprint-dashboard.component').then(
        (m) => m.SprintDashboardComponent
      ),
  },
];
