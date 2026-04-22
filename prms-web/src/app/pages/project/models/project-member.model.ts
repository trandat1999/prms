import { ProjectMemberRole } from './project-member.types';

export type ProjectMember = {
  id?: string;
  projectId?: string;
  projectDisplay?: string | null;
  userId?: string;
  userDisplay?: string | null;
  roleInProject?: ProjectMemberRole | null;
  allocationPercent?: number | null;
  isLead?: boolean | null;
  startDate?: string | Date | null;
  endDate?: string | Date | null;
  active?: boolean | null;
};

export type ProjectMemberWritePayload = Omit<ProjectMember, 'id' | 'projectDisplay' | 'userDisplay'>;
