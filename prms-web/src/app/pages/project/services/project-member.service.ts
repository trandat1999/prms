import { Injectable } from '@angular/core';
import { BaseService } from '../../../core/services/base-service';
import { ProjectMemberWritePayload } from '../models/project-member.model';
import { ProjectMemberSearchRequest } from '../models/project-member-search.request';

@Injectable({
  providedIn: 'root',
})
export class ProjectMemberService {
  private readonly apiUrl = '/api/v1/project-members';

  constructor(private base: BaseService) {}

  getPage(request: ProjectMemberSearchRequest) {
    return this.base.post(this.apiUrl + '/page', request);
  }

  getById(id: string) {
    return this.base.get(`${this.apiUrl}/${id}`);
  }

  create(request: ProjectMemberWritePayload) {
    return this.base.post(this.apiUrl, request);
  }

  update(id: string, request: ProjectMemberWritePayload) {
    return this.base.put(`${this.apiUrl}/${id}`, request);
  }

  delete(id: string) {
    return this.base.delete(`${this.apiUrl}/${id}`);
  }
}
