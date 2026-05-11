import { Injectable } from '@angular/core';
import { BaseService } from '../../../../core/services/base-service';
import { AppParamWritePayload } from '../models/app-param.model';
import { AppParamSearchRequest } from '../models/app-param-search.request';

@Injectable({
  providedIn: 'root',
})
export class AppParamService {
  private readonly apiUrl = '/api/v1/app-params';

  constructor(private base: BaseService) {}

  getPage(request: AppParamSearchRequest) {
    return this.base.post(this.apiUrl + '/page', request);
  }

  getById(id: string) {
    return this.base.get(`${this.apiUrl}/${id}`);
  }

  create(request: AppParamWritePayload) {
    return this.base.post(this.apiUrl, request);
  }

  update(id: string, request: AppParamWritePayload) {
    return this.base.put(`${this.apiUrl}/${id}`, request);
  }

  delete(id: string) {
    return this.base.delete(`${this.apiUrl}/${id}`);
  }
}
