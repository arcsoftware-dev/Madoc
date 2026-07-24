// In app.routes.ts
import { Routes } from '@angular/router';

export const routes: Routes = [
  { 
    path: '', 
    loadComponent: () => import('./home/home').then(m => m.Home) 
  },
  { 
    path: 'schedule', 
    loadComponent: () => import('./schedule-component/schedule-component').then(m => m.ScheduleComponent) 
  }
];