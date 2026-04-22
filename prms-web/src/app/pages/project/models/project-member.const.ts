import { ProjectMemberRole } from './project-member.types';

export const PROJECT_MEMBER_ROLE_OPTIONS: Array<{ value: ProjectMemberRole; label: string }> = [
  { value: 'PROJECT_MANAGER', label: 'Project Manager' },
  { value: 'TEAM_LEAD', label: 'Team Lead' },
  { value: 'BA', label: 'Business Analyst' },
  { value: 'DEVELOPER', label: 'Developer' },
  { value: 'TESTER', label: 'Tester' },
  { value: 'REVIEWER', label: 'Reviewer' },
  { value: 'OBSERVER', label: 'Observer' },
];

export function projectMemberRoleLabel(role?: ProjectMemberRole | null): string {
  return PROJECT_MEMBER_ROLE_OPTIONS.find((x) => x.value === role)?.label ?? (role ?? '—');
}
