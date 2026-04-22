import { ProjectMemberRole } from './project-member.types';

export type ProjectMemberSearchRequest = {
  keyword?: string | null;
  ids?: string[] | null;
  pageIndex?: number | null;
  pageSize?: number | null;
  voided?: boolean | null;
  projectId?: string | null;
  userId?: string | null;
  roleInProject?: ProjectMemberRole | null;
  active?: boolean | null;
};
