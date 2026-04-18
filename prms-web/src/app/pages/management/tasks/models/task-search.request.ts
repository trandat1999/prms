import { TaskStatus } from './task.types';

export type TaskSearchRequest = {
  keyword?: string | null;
  pageIndex?: number | null;
  pageSize?: number | null;
  voided?: boolean | null;
  ids?: string[] | null;
  projectId?: string | null;
  assignedId?: string | null;
  status?: TaskStatus | null;
  type?: string | null;
};

