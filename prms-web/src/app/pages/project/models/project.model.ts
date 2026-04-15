import { PriorityEnum, ProjectStatusEnum } from './project.types';

/** Payload gửi lên API tạo/cập nhật (không gửi managerName — chỉ đọc từ server). */
export type ProjectWritePayload = {
  code?: string;
  name?: string;
  description?: string | null;
  shortDescription?: string | null;
  managerId?: string | null;
  priority?: PriorityEnum | null;
  startDate?: string | null;
  endDate?: string | null;
  status?: ProjectStatusEnum | null;
  progressPercentage?: number | null;
};

export type Project = {
  id?: string;
  code?: string;
  name?: string;
  description?: string;
  shortDescription?: string;

  managerId?: string;
  managerName?: string;
  priority?: PriorityEnum;
  startDate?: string | Date;
  endDate?: string | Date;
  status?: ProjectStatusEnum;
  progressPercentage?: number;
};

