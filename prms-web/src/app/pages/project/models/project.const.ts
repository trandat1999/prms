import { PriorityEnum, ProjectStatusEnum } from './project.types';

export const PROJECT_STATUS_OPTIONS: Array<{ value: ProjectStatusEnum; label: string; color: string }> = [
  { value: 'NOT_STARTED', label: 'Chưa bắt đầu', color: 'default' },
  { value: 'IN_PROGRESS', label: 'Đang thực hiện', color: 'blue' },
  { value: 'COMPLETED', label: 'Đã hoàn thành', color: 'green' },
  { value: 'ON_HOLD', label: 'Tạm dừng', color: 'orange' },
  { value: 'CANCELLED', label: 'Đã hủy', color: 'red' },
];

export const PROJECT_PRIORITY_OPTIONS: Array<{ value: PriorityEnum; label: string; color: string }> = [
  { value: 'LOW', label: 'Thấp', color: 'green' },
  { value: 'MEDIUM', label: 'Trung bình', color: 'gold' },
  { value: 'HIGH', label: 'Cao', color: 'red' },
  { value: 'URGENT', label: 'Khẩn cấp', color: 'magenta' },
];

export function projectStatusLabel(status?: ProjectStatusEnum | null): string {
  return PROJECT_STATUS_OPTIONS.find((x) => x.value === status)?.label ?? '';
}

export function projectStatusColor(status?: ProjectStatusEnum | null): string {
  return PROJECT_STATUS_OPTIONS.find((x) => x.value === status)?.color ?? 'default';
}

export function projectPriorityLabel(priority?: PriorityEnum | null): string {
  return PROJECT_PRIORITY_OPTIONS.find((x) => x.value === priority)?.label ?? '';
}

export function projectPriorityColor(priority?: PriorityEnum | null): string {
  return PROJECT_PRIORITY_OPTIONS.find((x) => x.value === priority)?.color ?? 'default';
}

