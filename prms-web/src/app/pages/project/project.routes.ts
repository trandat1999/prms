import { Routes } from '@angular/router';
import { ProjectList } from './project-list/project-list';
import { ProjectTasks } from './project-tasks/project-tasks';

export const PROJECT_ROUTES: Routes = [
  { path: '', component: ProjectList },
  {
    path: ':projectId/tasks',
    component: ProjectTasks,
    data: { breadcrumb: 'breadcrumb.tasks' },
  },
];
