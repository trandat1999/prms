import { Routes } from '@angular/router';
import { TaskList } from './task-list/task-list';

export const TASK_ROUTES: Routes = [
  { path: '', component: TaskList, data: { breadcrumb: 'breadcrumb.tasks' } },
];

