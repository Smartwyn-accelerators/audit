import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { landingGuard } from './core/guards/landing.guard';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.authRoutes),
    canActivate: [landingGuard]
  },
  {
    path: 'home',
    loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent),
    canActivate: [authGuard]
  },
  {
    path: 'timesheet',
    loadChildren: () => import('./features/timesheet/timesheet.routes').then(m => m.timesheetRoutes),
    canActivate: [authGuard]
  },
  {
    path: '',
    redirectTo: '/auth/login',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: '/auth/login'
  }
];
