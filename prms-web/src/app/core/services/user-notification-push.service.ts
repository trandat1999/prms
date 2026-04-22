import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { catchError, firstValueFrom, of } from 'rxjs';
import { SwPush } from '@angular/service-worker';
import { AppConfigService } from './app-config-service';
import { StorageService } from './storage-service';
import { ApiResponse } from '../../shared/utils/api-response';

type PushSubscriptionUpsertRequest = {
  endpoint: string;
  expirationTime?: number | null;
  keys: { p256dh: string; auth: string };
};

@Injectable({ providedIn: 'root' })
export class UserNotificationPushService {
  private readonly apiUrl = '/api/v1/notifications/push';

  constructor(
    private readonly http: HttpClient,
    @Inject(SwPush) private readonly swPush: SwPush,
    private readonly appConfig: AppConfigService,
    private readonly storage: StorageService
  ) {}

  async ensureSubscribed(): Promise<void> {
    // Chỉ đăng ký khi user đã đăng nhập
    if (!this.storage.getToken()) {
      return;
    }
    if (!this.swPush.isEnabled) {
      return;
    }
    const publicKey = this.appConfig.pushPublicKey;
    if (!publicKey) {
      return;
    }

    const permission = await Notification.requestPermission();
    if (permission !== 'granted') {
      return;
    }

    const existing = await firstValueFrom(this.swPush.subscription);
    const sub =
      existing ??
      (await this.swPush.requestSubscription({
        serverPublicKey: publicKey,
      }));

    await firstValueFrom(this.upsert(sub));
  }

  /**
   * Gọi API void subscription trên server rồi hủy subscription trên trình duyệt.
   * Phải gọi khi vẫn còn access token (trước khi signOut).
   */
  async unsubscribeFromServer(): Promise<void> {
    if (!this.storage.getToken()) {
      return;
    }
    if (!this.swPush.isEnabled) {
      return;
    }
    try {
      const sub = await firstValueFrom(this.swPush.subscription);
      if (!sub) {
        return;
      }
      const endpoint = sub.endpoint;
      const url = `${this.appConfig.apiUrl}${this.apiUrl}/unsubscribe`;
      await firstValueFrom(
        this.http
          .delete<ApiResponse>(url, { body: { endpoint } })
          .pipe(catchError(() => of(null)))
      );
      await sub.unsubscribe();
    } catch {
      // ignore
    }
  }

  upsert(sub: PushSubscription) {
    const keysJson = sub.toJSON().keys;
    const body: PushSubscriptionUpsertRequest = {
      endpoint: sub.endpoint,
      expirationTime: (sub as any).expirationTime ?? null,
      keys: {
        p256dh: (keysJson?.['p256dh'] as string | undefined) || '',
        auth: (keysJson?.['auth'] as string | undefined) || '',
      },
    };
    const url = this.appConfig.apiUrl + this.apiUrl + '/subscribe';
    return this.http.post<ApiResponse>(url, body).pipe(
      catchError(() => of(null))
    );
  }
}
