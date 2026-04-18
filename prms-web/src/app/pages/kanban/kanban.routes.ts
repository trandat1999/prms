import { Routes } from '@angular/router';
import { TaskKanban } from './task-kanban/task-kanban';

export const KANBAN_ROUTES: Routes = [
  { path: '', component: TaskKanban, data: { breadcrumb: 'breadcrumb.kanban' } },
];

