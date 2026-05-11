import { Injectable } from '@angular/core';
import { BaseService } from '../../../core/services/base-service';
import { ResourceAllocationWritePayload } from '../models/resource-allocation.model';
import { ResourceAllocationSearchRequest } from '../models/resource-allocation-search.request';

@Injectable({
  providedIn: 'root',
})
export class ResourceAllocationService {
  private readonly apiUrl = '/api/v1/resource-allocations';

  constructor(private base: BaseService) {}

  getPage(request: ResourceAllocationSearchRequest) {
    return this.base.post(this.apiUrl + '/page', request);
  }

  getById(id: string) {
    return this.base.get(`${this.apiUrl}/${id}`);
  }

  create(request: ResourceAllocationWritePayload) {
    return this.base.post(this.apiUrl, request);
  }

  update(id: string, request: ResourceAllocationWritePayload) {
    return this.base.put(`${this.apiUrl}/${id}`, request);
  }

  delete(id: string) {
    return this.base.delete(`${this.apiUrl}/${id}`);
  }

  /** Xuất Excel theo tháng; backend chỉ áp dụng trường `month`. */
  exportExcel(request: { month: Date | string }) {
    return this.base.postBlob(`${this.apiUrl}/export/excel`, { month: request.month, voided: false });
  }
}
