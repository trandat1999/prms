import { TaskLogAction, TaskStatus } from './task.types';

export type Task = {
  id: string;
  code: string;
  name: string;
  shortDescription?: string | null;
  description?: string | null;
  projectId?: string | null;
  projectName?: string | null;
  status?: TaskStatus | null;
  kanbanOrder?: number | null;
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT' | null;
  estimatedHours?: number | null;
  actualHours?: number | null;
  assignedId?: string | null;
  assignedDisplay?: string | null;
  label?: string | null;
  type?: string | null;
};

export type TaskWritePayload = Omit<Task, 'id' | 'projectName' | 'assignedDisplay'>;

export type TaskAssignPayload = {
  assignedId: string;
};

export type TaskStatusPayload = {
  status: TaskStatus;
};

export type TaskLog = {
  id: string;
  taskId: string;
  action: TaskLogAction;
  oldValue?: string | null;
  newValue?: string | null;
  createdDate?: string | Date | null;
  createdBy?: string | null;
};

export type TaskKanbanColumn = {
  status: TaskStatus;
  name: string;
  tasks: Task[];
};

export type TaskKanbanBoard = {
  projectId: string;
  columns: TaskKanbanColumn[];
};

export type TaskKanbanColumnUpdatePayload = {
  status: TaskStatus;
  taskIds: string[];
};

export type TaskKanbanBoardUpdatePayload = {
  projectId: string;
  columns: TaskKanbanColumnUpdatePayload[];
};

