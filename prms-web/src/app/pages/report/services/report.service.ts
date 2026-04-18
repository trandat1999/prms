import { Injectable } from '@angular/core';
import { map } from 'rxjs';
import { BaseService } from '../../../core/services/base-service';
import { ApiResponse } from '../../../shared/utils/api-response';
import { ReportData, ReportFilter } from '../models/report.model';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly apiUrl = '/api/v1/reports';

  constructor(private base: BaseService) {}

  loadReport(filter: ReportFilter) {
    return this.base.post(`${this.apiUrl}/data`, filter).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        data: (res?.body ?? null) as ReportData | null,
      })),
    );
  }
}
