import { Injectable } from '@angular/core';
import { BaseService } from '../../../core/services/base-service';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly apiUrl = '/api/v1/dashboard';

  constructor(private base: BaseService) {}

  getOverview() {
    return this.base.get(`${this.apiUrl}/overview`);
  }
}
