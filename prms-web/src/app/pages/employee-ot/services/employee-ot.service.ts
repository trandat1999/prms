import { Injectable } from '@angular/core';
import { BaseService } from '../../../core/services/base-service';
import { EmployeeOtWritePayload } from '../models/employee-ot.model';
import { EmployeeOtSearchRequest } from '../models/employee-ot-search.request';

@Injectable({
  providedIn: 'root',
})
export class EmployeeOtService {
  private readonly apiUrl = '/api/v1/employee-ots';

  constructor(private base: BaseService) {}

  getPage(request: EmployeeOtSearchRequest) {
    return this.base.post(this.apiUrl + '/page', request);
  }

  getById(id: string) {
    return this.base.get(`${this.apiUrl}/${id}`);
  }

  create(request: EmployeeOtWritePayload) {
    return this.base.post(this.apiUrl, request);
  }

  update(id: string, request: EmployeeOtWritePayload) {
    return this.base.put(`${this.apiUrl}/${id}`, request);
  }

  delete(id: string) {
    return this.base.delete(`${this.apiUrl}/${id}`);
  }

  exportMonthlyReport(request: {
    month: string;
    keyword?: string | null;
    userId?: string | null;
    projectId?: string | null;
    status?: string | null;
    otType?: string | null;
  }) {
    return this.base.postBlob(this.apiUrl + '/report/monthly', request);
  }
}
