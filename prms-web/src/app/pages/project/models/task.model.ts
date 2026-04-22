import { TaskLogAction, TaskStatus } from './task.types';

export type TaskChecklistItem = {
  id?: string | null;
  title: string;
  checked?: boolean | null;
  sortOrder?: number | null;
  estimatedHours?: number | null;
};

export type TaskRef = {
  id: string;
  code?: string | null;
  name?: string | null;
};

export type Task = {
  id: string;
  code: string;
  name: string;
  shortDescription?: string | null;
  description?: string | null;
  projectId?: string | null;
  projectName?: string | null;
  projectManagerId?: string | null;
  status?: TaskStatus | null;
  kanbanOrder?: number | null;
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT' | null;
  estimatedHours?: number | null;
  actualHours?: number | null;
  assignedId?: string | null;
  assignedDisplay?: string | null;
  reporterId?: string | null;
  reporterDisplay?: string | null;
  reviewerId?: string | null;
  reviewerDisplay?: string | null;
  parentTaskId?: string | null;
  parentTaskCode?: string | null;
  dueDate?: string | Date | null;
  startedAt?: string | Date | null;
  completedAt?: string | Date | null;
  blockedReason?: string | null;
  taskCategory?: string | null;
  storyPoint?: number | null;
  label?: string | null;
  type?: string | null;
  checklists?: TaskChecklistItem[] | null;
  predecessors?: TaskRef[] | null;
  predecessorTaskIds?: string[] | null;
  checklistTotalCount?: number | null;
  checklistDoneCount?: number | null;
};

export type TaskWritePayload = Omit<
  Task,
  | 'id'
  | 'projectName'
  | 'assignedDisplay'
  | 'reporterDisplay'
  | 'reviewerDisplay'
  | 'parentTaskCode'
  | 'predecessors'
>;

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
