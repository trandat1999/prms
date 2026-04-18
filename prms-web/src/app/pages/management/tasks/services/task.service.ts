import { Injectable } from '@angular/core';
import { map } from 'rxjs';
import { BaseService } from '../../../../core/services/base-service';
import { ApiResponse } from '../../../../shared/utils/api-response';
import { Page } from '../../../project/models/page.model';
import {
  Task,
  TaskAssignPayload,
  TaskKanbanBoard,
  TaskKanbanBoardUpdatePayload,
  TaskLog,
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
    return this.base.post(this.apiUrl + '/page', request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        page: (res?.body ?? null) as Page<Task> | null,
      }))
    );
  }

  getById(id: string) {
    return this.base.get(`${this.apiUrl}/${id}`).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        task: (res?.body ?? null) as Task | null,
      }))
    );
  }

  create(request: TaskWritePayload) {
    return this.base.post(this.apiUrl, request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        task: (res?.body ?? null) as Task | null,
      }))
    );
  }

  update(id: string, request: TaskWritePayload) {
    return this.base.put(`${this.apiUrl}/${id}`, request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        task: (res?.body ?? null) as Task | null,
      }))
    );
  }

  delete(id: string) {
    return this.base.delete(`${this.apiUrl}/${id}`).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        task: (res?.body ?? null) as Task | null,
      }))
    );
  }

  assign(id: string, request: TaskAssignPayload) {
    return this.base.patch(`${this.apiUrl}/${id}/assign`, request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        task: (res?.body ?? null) as Task | null,
      }))
    );
  }

  updateStatus(id: string, request: TaskStatusPayload) {
    return this.base.patch(`${this.apiUrl}/${id}/status`, request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        task: (res?.body ?? null) as Task | null,
      }))
    );
  }

  getLogs(id: string) {
    return this.base.get(`${this.apiUrl}/${id}/logs`).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        logs: (res?.body ?? []) as TaskLog[],
      }))
    );
  }

  getKanbanBoard(projectId: string) {
    return this.base.get(`${this.apiUrl}/kanban/board?projectId=${projectId}`).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        board: (res?.body ?? null) as TaskKanbanBoard | null,
      }))
    );
  }

  updateKanbanBoard(request: TaskKanbanBoardUpdatePayload) {
    return this.base.put(`${this.apiUrl}/kanban/board`, request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        ok: (res?.body ?? null) as boolean | null,
      }))
    );
  }
}

