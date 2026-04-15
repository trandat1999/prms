import {Routes} from '@angular/router';
import {ProjectList} from './project-list/project-list';

export const PROJECT_ROUTES: Routes = [
  { path: '', component: ProjectList , data: {breadcrumb: 'breadcrumb.project'}},
];
