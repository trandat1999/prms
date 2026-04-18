import { Routes } from '@angular/router';
import { AppParamList } from './app-param-list/app-param-list';

export const APP_PARAM_ROUTES: Routes = [
  { path: '', component: AppParamList, data: { breadcrumb: 'breadcrumb.appParams' } },
];

