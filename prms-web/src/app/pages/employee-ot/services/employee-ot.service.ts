import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { map } from 'rxjs';
import { BaseService } from '../../../core/services/base-service';
import { ApiResponse } from '../../../shared/utils/api-response';
import { Page } from '../../project/models/page.model';
import { EmployeeOt, EmployeeOtWritePayload } from '../models/employee-ot.model';
import { EmployeeOtSearchRequest } from '../models/employee-ot-search.request';

@Injectable({
  providedIn: 'root',
})
export class EmployeeOtService {
  private readonly apiUrl = '/api/v1/employee-ots';

  constructor(private base: BaseService) {}

  getPage(request: EmployeeOtSearchRequest) {
    return this.base.post(this.apiUrl + '/page', request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        page: (res?.body ?? null) as Page<EmployeeOt> | null,
      }))
    );
  }

  getById(id: string) {
    return this.base.get(`${this.apiUrl}/${id}`).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        row: (res?.body ?? null) as EmployeeOt | null,
      }))
    );
  }

  create(request: EmployeeOtWritePayload) {
    return this.base.post(this.apiUrl, request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        row: (res?.body ?? null) as EmployeeOt | null,
      }))
    );
  }

  update(id: string, request: EmployeeOtWritePayload) {
    return this.base.put(`${this.apiUrl}/${id}`, request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        row: (res?.body ?? null) as EmployeeOt | null,
      }))
    );
  }

  delete(id: string) {
    return this.base.delete(`${this.apiUrl}/${id}`).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        row: (res?.body ?? null) as EmployeeOt | null,
      }))
    );
  }

  exportMonthlyReport(request: {
    month: string;
    keyword?: string | null;
    userId?: string | null;
    projectId?: string | null;
    status?: string | null;
    otType?: string | null;
  }) {
    return this.base.postBlob(this.apiUrl + '/report/monthly', request).pipe(map((res: HttpResponse<Blob>) => res));
  }
}
