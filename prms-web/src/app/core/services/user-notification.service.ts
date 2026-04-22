import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, map, of } from 'rxjs';
import { AppConfigService } from './app-config-service';
import { BaseService } from './base-service';
import { ApiResponse } from '../../shared/utils/api-response';
import { Page } from '../../pages/project/models/page.model';

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
      map((res) => ({
        raw: res,
        count: typeof res?.body === 'number' ? (res.body as number) : 0,
      })),
      catchError(() => of({ raw: undefined, count: 0 }))
    );
  }

  getPage(request: UserNotificationSearchRequest) {
    return this.base.post(this.apiUrl + '/page', request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        page: (res?.body ?? null) as Page<UserNotificationItem> | null,
      }))
    );
  }

  markRead(id: string) {
    return this.base.patch(`${this.apiUrl}/${id}/read`, {}).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        item: (res?.body ?? null) as UserNotificationItem | null,
      }))
    );
  }
}
