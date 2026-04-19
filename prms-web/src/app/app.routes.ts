import {Routes} from '@angular/router';
import {authGuard} from './core/guards/auth-guard';
import {loginGuard} from './core/guards/login-guard';
import {MainLayout} from './shared/layout/main-layout/main-layout';
import {Login} from './pages/auth/login/login';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: '/dashboard' },
  { path: '', component: MainLayout, canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        data: { breadcrumb: 'breadcrumb.dashboard' },
        loadChildren: () => import('./pages/dashboard/dashboard.routes').then((m) => m.DASHBOARD_ROUTES),
      },
      {
        path: 'report',
        data: { breadcrumb: 'breadcrumb.report' },
        loadChildren: () => import('./pages/report/report.routes').then((m) => m.REPORT_ROUTES),
      },
      {
        path: 'project',
        data: { breadcrumb: 'breadcrumb.project' },
        loadChildren: () => import('./pages/project/project.routes').then((m) => m.PROJECT_ROUTES),
      },
      {
        path: 'resource-allocation',
        data: { breadcrumb: 'breadcrumb.resourceAllocation' },
        loadChildren: () =>
          import('./pages/resource-allocation/resource-allocation.routes').then((m) => m.RESOURCE_ALLOCATION_ROUTES),
      },
      {
        path: 'employee-ot',
        data: { breadcrumb: 'breadcrumb.employeeOt' },
        loadChildren: () => import('./pages/employee-ot/employee-ot.routes').then((m) => m.EMPLOYEE_OT_ROUTES),
      },
      {
        path: 'kanban',
        data: { breadcrumb: 'breadcrumb.kanban' },
        loadChildren: () => import('./pages/kanban/kanban.routes').then((m) => m.KANBAN_ROUTES),
      }
      ,
      {
        path: 'management',
        data: { breadcrumb: 'breadcrumb.management' },
        loadChildren: () => import('./pages/management/management.routes').then((m) => m.MANAGEMENT_ROUTES),
      }
    ]
  },
  { path: 'login', component: Login, canActivate: [loginGuard] },
];
