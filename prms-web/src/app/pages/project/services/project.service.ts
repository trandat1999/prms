import { Injectable } from '@angular/core';
import { map } from 'rxjs';
import { BaseService } from '../../../core/services/base-service';
import { ApiResponse } from '../../../shared/utils/api-response';
import { Page } from '../models/page.model';
import { Project, ProjectWritePayload } from '../models/project.model';
import { ProjectSearchRequest } from '../models/project-search.request';

@Injectable({
  providedIn: 'root',
})
export class ProjectService {
  private readonly apiUrl = '/api/v1/projects';

  constructor(private base: BaseService) {}

  getPage(request: ProjectSearchRequest) {
    return this.base.post(this.apiUrl + '/page', request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        page: (res?.body ?? null) as Page<Project> | null,
      }))
    );
  }

  create(request: ProjectWritePayload) {
    return this.base.post(this.apiUrl, request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        project: (res?.body ?? null) as Project | null,
      }))
    );
  }

  getById(id: string) {
    return this.base.get(`${this.apiUrl}/${id}`).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        project: (res?.body ?? null) as Project | null,
      }))
    );
  }

  update(id: string, request: ProjectWritePayload) {
    return this.base.put(`${this.apiUrl}/${id}`, request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        project: (res?.body ?? null) as Project | null,
      }))
    );
  }

  delete(id: string) {
    return this.base.delete(`${this.apiUrl}/${id}`).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        project: (res?.body ?? null) as Project | null,
      }))
    );
  }
}

