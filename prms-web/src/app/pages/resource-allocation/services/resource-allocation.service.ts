import { Injectable } from '@angular/core';
import { map } from 'rxjs';
import { BaseService } from '../../../core/services/base-service';
import { ApiResponse } from '../../../shared/utils/api-response';
import { Page } from '../../project/models/page.model';
import { ResourceAllocation, ResourceAllocationWritePayload } from '../models/resource-allocation.model';
import { ResourceAllocationSearchRequest } from '../models/resource-allocation-search.request';

@Injectable({
  providedIn: 'root',
})
export class ResourceAllocationService {
  private readonly apiUrl = '/api/v1/resource-allocations';

  constructor(private base: BaseService) {}

  getPage(request: ResourceAllocationSearchRequest) {
    return this.base.post(this.apiUrl + '/page', request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        page: (res?.body ?? null) as Page<ResourceAllocation> | null,
      }))
    );
  }

  getById(id: string) {
    return this.base.get(`${this.apiUrl}/${id}`).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        row: (res?.body ?? null) as ResourceAllocation | null,
      }))
    );
  }

  create(request: ResourceAllocationWritePayload) {
    return this.base.post(this.apiUrl, request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        row: (res?.body ?? null) as ResourceAllocation | null,
      }))
    );
  }

  update(id: string, request: ResourceAllocationWritePayload) {
    return this.base.put(`${this.apiUrl}/${id}`, request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        row: (res?.body ?? null) as ResourceAllocation | null,
      }))
    );
  }

  delete(id: string) {
    return this.base.delete(`${this.apiUrl}/${id}`).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        row: (res?.body ?? null) as ResourceAllocation | null,
      }))
    );
  }

  /** Xuất Excel theo tháng; backend chỉ áp dụng trường `month`. */
  exportExcel(request: { month: Date | string }) {
    return this.base.postBlob(`${this.apiUrl}/export/excel`, { month: request.month, voided: false });
  }
}
