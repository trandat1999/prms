import { Injectable } from '@angular/core';
import { BaseService } from '../../../../core/services/base-service';
import { SkillWritePayload } from '../models/skill.model';
import { SkillSearchRequest } from '../models/skill-search.request';

@Injectable({
  providedIn: 'root',
})
export class SkillService {
  private readonly apiUrl = '/api/v1/skills';

  constructor(private base: BaseService) {}

  getPage(request: SkillSearchRequest) {
    return this.base.post(this.apiUrl + '/page', request);
  }

  getById(id: string) {
    return this.base.get(`${this.apiUrl}/${id}`);
  }

  create(request: SkillWritePayload) {
    return this.base.post(this.apiUrl, request);
  }

  update(id: string, request: SkillWritePayload) {
    return this.base.put(`${this.apiUrl}/${id}`, request);
  }

  delete(id: string) {
    return this.base.delete(`${this.apiUrl}/${id}`);
  }
}

