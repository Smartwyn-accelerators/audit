import { Routes } from '@angular/router';

export const timesheetRoutes: Routes = [
  {
    path: 'customers',
    loadComponent: () => import('../customers/customers.component').then(m => m.CustomersComponent)
  },
  {
    path: 'projects',
    loadComponent: () => import('../projects/projects.component').then(m => m.ProjectsComponent)
  },
  {
    path: 'overview',
    loadComponent: () => import('../overview/overview.component').then(m => m.OverviewComponent)
  },
  {
    path: 'details',
    loadComponent: () => import('../details/details.component').then(m => m.DetailsComponent)
  },
  {
    path: 'timeOff',
    loadComponent: () => import('../time-off/time-off.component').then(m => m.TimeOffComponent)
  },
  {
    path: 'audit',
    loadComponent: () => import('../layout/timesheet-layout.component').then(m => m.TimesheetLayoutComponent),
    children: [
      {
        path: '',
        loadComponent: () => import('audit-library').then(m => m.AuditComponent)
      }
    ]
  },
  {
    path: 'scheduler',
    children: [
      {
        path: 'jobs',
        loadComponent: () => import('../scheduler/jobs/jobs.component').then(m => m.JobsComponent)
      },
      {
        path: 'executingJobs',
        loadComponent: () => import('../scheduler/executing-jobs/executing-jobs.component').then(m => m.ExecutingJobsComponent)
      },
      {
        path: 'executionHistory',
        loadComponent: () => import('../scheduler/execution-history/execution-history.component').then(m => m.ExecutionHistoryComponent)
      },
      {
        path: 'triggers',
        loadComponent: () => import('../scheduler/triggers/triggers.component').then(m => m.TriggersComponent)
      }
    ]
  },
  {
    path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  }
];
