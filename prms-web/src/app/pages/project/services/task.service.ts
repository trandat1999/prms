import { Injectable } from '@angular/core';
import { BaseService } from '../../../core/services/base-service';
import {
  TaskAssignPayload,
  TaskKanbanBoardUpdatePayload,
  TaskStatusPayload,
  TaskWritePayload,
} from '../models/task.model';
import { TaskSearchRequest } from '../models/task-search.request';

@Injectable({
  providedIn: 'root',
})
export class TaskService {
  private readonly apiUrl = '/api/v1/tasks';

  constructor(private base: BaseService) {}

  getPage(request: TaskSearchRequest) {
    return this.base.post(this.apiUrl + '/page', request);
  }

  getById(id: string) {
    return this.base.get(`${this.apiUrl}/${id}`);
  }

  create(request: TaskWritePayload) {
    return this.base.post(this.apiUrl, request);
  }

  update(id: string, request: TaskWritePayload) {
    return this.base.put(`${this.apiUrl}/${id}`, request);
  }

  delete(id: string) {
    return this.base.delete(`${this.apiUrl}/${id}`);
  }

  assign(id: string, request: TaskAssignPayload) {
    return this.base.patch(`${this.apiUrl}/${id}/assign`, request);
  }

  updateStatus(id: string, request: TaskStatusPayload) {
    return this.base.patch(`${this.apiUrl}/${id}/status`, request);
  }

  getLogs(id: string) {
    return this.base.get(`${this.apiUrl}/${id}/logs`);
  }

  getKanbanBoard(projectId: string) {
    const qs = projectId ? `?projectId=${projectId}` : '';
    return this.base.get(`${this.apiUrl}/kanban/board${qs}`);
  }

  updateKanbanBoard(request: TaskKanbanBoardUpdatePayload) {
    return this.base.put(`${this.apiUrl}/kanban/board`, request);
  }

  getChecklists(taskId: string) {
    return this.base.get(`${this.apiUrl}/${taskId}/checklists`);
  }

  toggleChecklist(taskId: string, checklistId: string, checked: boolean) {
    return this.base.patch(`${this.apiUrl}/${taskId}/checklists/${checklistId}`, { checked });
  }
}
