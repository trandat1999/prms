import { Injectable } from '@angular/core';
import { BaseService } from '../../../core/services/base-service';
import { ProjectWritePayload } from '../models/project.model';
import { ProjectSearchRequest } from '../models/project-search.request';

@Injectable({
  providedIn: 'root',
})
export class ProjectService {
  private readonly apiUrl = '/api/v1/projects';

  constructor(private base: BaseService) {}

  getPage(request: ProjectSearchRequest) {
    return this.base.post(this.apiUrl + '/page', request);
  }

  create(request: ProjectWritePayload) {
    return this.base.post(this.apiUrl, request);
  }

  getById(id: string) {
    return this.base.get(`${this.apiUrl}/${id}`);
  }

  update(id: string, request: ProjectWritePayload) {
    return this.base.put(`${this.apiUrl}/${id}`, request);
  }

  delete(id: string) {
    return this.base.delete(`${this.apiUrl}/${id}`);
  }
}
