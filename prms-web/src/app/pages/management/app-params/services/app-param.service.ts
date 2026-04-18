import { Injectable } from '@angular/core';
import { map } from 'rxjs';
import { BaseService } from '../../../../core/services/base-service';
import { ApiResponse } from '../../../../shared/utils/api-response';
import { Page } from '../../../project/models/page.model';
import { AppParam, AppParamWritePayload } from '../models/app-param.model';
import { AppParamSearchRequest } from '../models/app-param-search.request';

@Injectable({
  providedIn: 'root',
})
export class AppParamService {
  private readonly apiUrl = '/api/v1/app-params';

  constructor(private base: BaseService) {}

  getPage(request: AppParamSearchRequest) {
    return this.base.post(this.apiUrl + '/page', request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        page: (res?.body ?? null) as Page<AppParam> | null,
      }))
    );
  }

  getById(id: string) {
    return this.base.get(`${this.apiUrl}/${id}`).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        item: (res?.body ?? null) as AppParam | null,
      }))
    );
  }

  create(request: AppParamWritePayload) {
    return this.base.post(this.apiUrl, request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        item: (res?.body ?? null) as AppParam | null,
      }))
    );
  }

  update(id: string, request: AppParamWritePayload) {
    return this.base.put(`${this.apiUrl}/${id}`, request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        item: (res?.body ?? null) as AppParam | null,
      }))
    );
  }

  delete(id: string) {
    return this.base.delete(`${this.apiUrl}/${id}`).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        item: (res?.body ?? null) as AppParam | null,
      }))
    );
  }
}

