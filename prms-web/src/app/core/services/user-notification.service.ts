import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, of } from 'rxjs';
import { AppConfigService } from './app-config-service';
import { BaseService } from './base-service';
import { ApiResponse } from '../../shared/utils/api-response';

export type UserNotificationItem = {
  id: string;
  message: string;
  read: boolean;
  createdDate?: string | null;
  relatedProjectId?: string | null;
  relatedTaskId?: string | null;
  relatedTaskCode?: string | null;
};

export type UserNotificationSearchRequest = {
  pageIndex?: number;
  pageSize?: number;
  read?: boolean | null;
  voided?: boolean;
};

@Injectable({
  providedIn: 'root',
})
export class UserNotificationService {
  private readonly apiUrl = '/api/v1/notifications';

  constructor(
    private readonly http: HttpClient,
    private readonly appConfig: AppConfigService,
    private readonly base: BaseService
  ) {}

  /** Không dùng spinner toàn cục — gọi định kỳ trên header */
  unreadCount() {
    const url = this.appConfig.apiUrl + this.apiUrl + '/unread-count';
    return this.http.get<ApiResponse>(url).pipe(
      catchError(() => of({ body: 0 } as ApiResponse))
    );
  }

  getPage(request: UserNotificationSearchRequest) {
    return this.base.post(this.apiUrl + '/page', request);
  }

  markRead(id: string) {
    return this.base.patch(`${this.apiUrl}/${id}/read`, {});
  }
}
