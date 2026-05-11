import { Injectable } from '@angular/core';
import { BaseService } from '../../../core/services/base-service';
import { ReportFilter } from '../models/report.model';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly apiUrl = '/api/v1/reports';

  constructor(private base: BaseService) {}

  loadReport(filter: ReportFilter) {
    return this.base.post(`${this.apiUrl}/data`, filter);
  }
}
