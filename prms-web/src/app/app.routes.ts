import {Routes} from '@angular/router';
import {authGuard} from './core/guards/auth-guard';
import {loginGuard} from './core/guards/login-guard';
import {MainLayout} from './shared/layout/main-layout/main-layout';
import {Login} from './pages/auth/login/login';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: '/welcome' },
  { path: '', component: MainLayout, canActivate: [authGuard],
    children: [
      { path: 'welcome', loadChildren: () => import('./pages/welcome/welcome.routes').then(m => m.WELCOME_ROUTES) },
      { path: 'project', 
        data: {
          breadcrumb: 'breadcrumb.none',
        },
        loadChildren: () => import('./pages/project/project.routes').then(m => m.PROJECT_ROUTES) }
    ]
  },
  { path: 'login', component: Login, canActivate: [loginGuard] },
];
