import { Routes } from '@angular/router';

export const MANAGEMENT_ROUTES: Routes = [
  {
    path: 'users',
    loadChildren: () =>
      import('./users/user.routes').then((m) => m.USER_MANAGEMENT_ROUTES),
    data: { breadcrumb: 'breadcrumb.users' },
  },
  {
    path: 'app-params',
    loadChildren: () =>
      import('./app-params/app-param.routes').then((m) => m.APP_PARAM_ROUTES),
    data: { breadcrumb: 'breadcrumb.appParams' },
  },
  { path: '', pathMatch: 'full', redirectTo: 'users' },
];

