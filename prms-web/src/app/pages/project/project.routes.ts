import { Routes } from '@angular/router';
import { ProjectList } from './project-list/project-list';
import { ProjectDetail } from './project-detail/project-detail';
import { ProjectTasks } from './project-tasks/project-tasks';
import { ProjectMembers } from './project-members/project-members';

export const PROJECT_ROUTES: Routes = [
  { path: '', component: ProjectList },
  {
    path: ':projectId',
    component: ProjectDetail,
    data: { breadcrumb: 'breadcrumb.projectDetail' },
  },
  {
    path: ':projectId/tasks',
    component: ProjectTasks,
    data: { breadcrumb: 'breadcrumb.tasks' },
  },
  {
    path: ':projectId/team',
    component: ProjectMembers,
    data: { breadcrumb: 'breadcrumb.projectTeam' },
  },
];
