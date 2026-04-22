import { Injectable } from '@angular/core';
import { map } from 'rxjs';
import { BaseService } from '../../../core/services/base-service';
import { ApiResponse } from '../../../shared/utils/api-response';
import { Page } from '../models/page.model';
import {
  ProjectMember,
  ProjectMemberWritePayload,
} from '../models/project-member.model';
import { ProjectMemberSearchRequest } from '../models/project-member-search.request';

@Injectable({
  providedIn: 'root',
})
export class ProjectMemberService {
  private readonly apiUrl = '/api/v1/project-members';

  constructor(private base: BaseService) {}

  getPage(request: ProjectMemberSearchRequest) {
    return this.base.post(this.apiUrl + '/page', request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        page: (res?.body ?? null) as Page<ProjectMember> | null,
      }))
    );
  }

  getById(id: string) {
    return this.base.get(`${this.apiUrl}/${id}`).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        row: (res?.body ?? null) as ProjectMember | null,
      }))
    );
  }

  create(request: ProjectMemberWritePayload) {
    return this.base.post(this.apiUrl, request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        row: (res?.body ?? null) as ProjectMember | null,
      }))
    );
  }

  update(id: string, request: ProjectMemberWritePayload) {
    return this.base.put(`${this.apiUrl}/${id}`, request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        row: (res?.body ?? null) as ProjectMember | null,
      }))
    );
  }

  delete(id: string) {
    return this.base.delete(`${this.apiUrl}/${id}`).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        row: (res?.body ?? null) as ProjectMember | null,
      }))
    );
  }
}
