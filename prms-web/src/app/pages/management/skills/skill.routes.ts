import { Routes } from '@angular/router';
import { SkillList } from './skill-list/skill-list';

export const SKILL_ROUTES: Routes = [
  { path: '', component: SkillList, data: { breadcrumb: 'breadcrumb.skills' } },
];

