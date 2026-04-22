import { Injectable, NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { AppConfigService } from './app-config-service';
import { StorageService } from './storage-service';
import { UserNotificationItem } from './user-notification.service';

export type UserNotificationStreamEvent =
  | { type: 'ready' }
  | { type: 'unreadCount'; unreadCount: number }
  | { type: 'notification'; unreadCount: number; item: UserNotificationItem };

@Injectable({ providedIn: 'root' })
export class UserNotificationStreamService {
  private eventSource?: EventSource;

  constructor(
    private readonly appConfig: AppConfigService,
    private readonly storage: StorageService,
    private readonly zone: NgZone
  ) {}

  connect(): Observable<UserNotificationStreamEvent> {
    return new Observable<UserNotificationStreamEvent>((observer) => {
      const token = this.storage.getToken();
      if (!token) {
        observer.complete();
        return () => {};
      }

      // EventSource không set được Authorization header => truyền qua query param (BE chỉ cho phép ở /api/v1/notifications/stream)
      const url = `${this.appConfig.apiUrl}/api/v1/notifications/stream?accessToken=${encodeURIComponent(token)}`;
      this.eventSource = new EventSource(url, { withCredentials: true } as any);

      const onReady = () => this.zone.run(() => observer.next({ type: 'ready' }));
      const onUnread = (e: MessageEvent) => {
        try {
          const data = JSON.parse(e.data || '{}');
          this.zone.run(() =>
            observer.next({ type: 'unreadCount', unreadCount: Number(data?.unreadCount ?? 0) })
          );
        } catch {
          // ignore
        }
      };
      const onNotification = (e: MessageEvent) => {
        try {
          const data = JSON.parse(e.data || '{}');
          this.zone.run(() =>
            observer.next({
              type: 'notification',
              unreadCount: Number(data?.unreadCount ?? 0),
              item: (data?.item ?? null) as UserNotificationItem,
            })
          );
        } catch {
          // ignore
        }
      };
      const onError = () => this.zone.run(() => observer.error(new Error('SSE disconnected')));

      this.eventSource.addEventListener('ready', onReady as any);
      this.eventSource.addEventListener('unreadCount', onUnread as any);
      this.eventSource.addEventListener('notification', onNotification as any);
      this.eventSource.onerror = onError as any;

      return () => {
        try {
          this.eventSource?.close();
        } catch {
          // ignore
        }
        this.eventSource = undefined;
      };
    });
  }

  disconnect(): void {
    try {
      this.eventSource?.close();
    } catch {
      // ignore
    }
    this.eventSource = undefined;
  }
}

