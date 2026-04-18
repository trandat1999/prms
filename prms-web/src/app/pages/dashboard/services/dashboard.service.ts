import { Injectable } from '@angular/core';
import { map } from 'rxjs';
import { BaseService } from '../../../core/services/base-service';
import { ApiResponse } from '../../../shared/utils/api-response';
import { DashboardOverview } from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly apiUrl = '/api/v1/dashboard';

  constructor(private base: BaseService) {}

  getOverview() {
    return this.base.get(`${this.apiUrl}/overview`).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        overview: (res?.body ?? null) as DashboardOverview | null,
      })),
    );
  }
}
