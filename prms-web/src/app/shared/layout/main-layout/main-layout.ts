import { Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {AuthService} from '../../../pages/auth/auth-service';
import {Router, RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {SignalService} from '../../../core/services/signal-service';
import {StoreService} from '../../../core/services/store-service';
import {StorageService} from '../../../core/services/storage-service';
import {AppConfigService} from '../../../core/services/app-config-service';
import {UserNotificationService, UserNotificationItem} from '../../../core/services/user-notification.service';
import {BehaviorSubject, timer} from 'rxjs';
import {UserNotificationStreamService} from '../../../core/services/user-notification-stream.service';
import {NzNotificationService} from 'ng-zorro-antd/notification';
import {UserNotificationPushService} from '../../../core/services/user-notification-push.service';
import {navigation, NavigationItem} from '../../utils/navigation.model';
import {NzContentComponent, NzHeaderComponent, NzLayoutComponent, NzSiderComponent} from 'ng-zorro-antd/layout';
import {NzMenuDirective, NzMenuItemComponent, NzSubMenuComponent} from 'ng-zorro-antd/menu';
import {NzIconDirective} from 'ng-zorro-antd/icon';
import {NzOptionComponent, NzSelectComponent} from 'ng-zorro-antd/select';
import {FormsModule} from '@angular/forms';
import {NzAvatarComponent} from 'ng-zorro-antd/avatar';
import {NzDropDownDirective, NzDropdownMenuComponent} from 'ng-zorro-antd/dropdown';
import {NzBadgeComponent} from 'ng-zorro-antd/badge';
import {Breadcrumb} from '../../breadcrumb/breadcrumb';
import {AsyncPipe, NgOptimizedImage} from '@angular/common';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-main-layout',
  imports: [
    NzLayoutComponent,
    NzSiderComponent,
    NzMenuDirective,
    NzMenuItemComponent,
    RouterLink,
    NzIconDirective,
    TranslatePipe,
    NzSubMenuComponent,
    RouterLinkActive,
    NzHeaderComponent,
    NzSelectComponent,
    NzOptionComponent,
    FormsModule,
    NzAvatarComponent,
    NzDropDownDirective,
    NzDropdownMenuComponent,
    NzBadgeComponent,
    NzContentComponent,
    RouterOutlet,
    Breadcrumb,
    NgOptimizedImage,
    AsyncPipe
  ],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.scss',
})
export class MainLayout {
  private readonly destroyRef = inject(DestroyRef);
  private readonly baseTabTitle = document.title || 'PRMS';

  isCollapsed = false;
  currentLanguage = "en";
  currentUser: any;
  navigation = navigation;

  notifUnread = 0;
  notifLoading = false;
  notifItems: UserNotificationItem[] = [];
  /** Dropdown thông báo đang mở — không toast SSE khi mở để tránh trùng với danh sách */
  notifDropdownOpen = false;

  constructor(private authService: AuthService,
              private router: Router,
              private translate: TranslateService,
              private signalService: SignalService,
              private appConfigService: AppConfigService,
              private store: StoreService,
              private storage: StorageService,
              private userNotificationService: UserNotificationService,
              private userNotificationStream: UserNotificationStreamService,
              private toast: NzNotificationService,
              private userNotificationPushService: UserNotificationPushService,
              private title: Title) {
    this.currentLanguage = this.storage.getLanguage();
    this.store.getCurrentUser().subscribe(user => {
      this.currentUser = user;
    });
    this.signalService.subscribeToSignal().subscribe(signal => {
      if(signal.type === 'navCollapsed'){
        this.isCollapsed = signal.value || false;
      }
    });
    // Fallback polling (nhẹ hơn) để đảm bảo badge vẫn đúng khi SSE bị chặn bởi network/proxy
    timer(0, 300_000)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.refreshUnreadCount());
    this.refreshUnreadCount();

    this.userNotificationStream
      .connect()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (e) => {
          if (e.type === 'unreadCount') {
            this.notifUnread = e.unreadCount;
            this.syncTabTitle();
          }
          if (e.type === 'notification') {
            this.notifUnread = e.unreadCount;
            this.syncTabTitle();
            if (!this.notifDropdownOpen && e.item?.message) {
              this.toast.info('Thông báo', e.item.message);
            }
          }
        },
        error: () => {
          // ignore: fallback polling vẫn chạy
        }
      });

    // Web Push: nhận thông báo ngay cả khi tab đóng (OS notification)
    this.userNotificationPushService.ensureSubscribed().catch(() => {
      // ignore
    });
  }

  onNotifDropdownVisible(open: boolean): void {
    this.notifDropdownOpen = open;
    if (!open) {
      return;
    }
    this.notifLoading = true;
    this.userNotificationService
      .getPage({ pageIndex: 0, pageSize: 25, voided: false })
      .subscribe(({ raw, page }) => {
        this.notifLoading = false;
        if (raw?.code === 200) {
          this.notifItems = page?.content ?? [];
        }
      });
  }

  openNotification(item: UserNotificationItem): void {
    // Optimistic UI: đánh dấu đã đọc ngay để user nhận biết
    const local = this.notifItems.find((x) => x.id === item.id);
    if (local) {
      local.read = true;
    }
    this.userNotificationService.markRead(item.id).subscribe(({ raw }) => {
      if (raw?.code === 200) {
        this.refreshUnreadCount();
      }
      // Điều hướng:
      // - Thông báo liên quan task => mở Kanban (lọc theo project nếu có)
      // - Thông báo khác liên quan project => vào danh sách task của project
      if (item.relatedTaskId || item.relatedTaskCode) {
        this.router.navigate(['/kanban'], {
          queryParams: {
            projectId: item.relatedProjectId ?? undefined,
            taskId: item.relatedTaskId ?? undefined,
            taskCode: item.relatedTaskCode ?? undefined,
          },
        });
        return;
      }
      if (item.relatedProjectId) {
        this.router.navigate(['/project', item.relatedProjectId, 'tasks']);
      }
    });
  }

  private refreshUnreadCount(): void {
    this.userNotificationService.unreadCount().subscribe(({ count }) => {
      this.notifUnread = count;
      this.syncTabTitle();
    });
  }

  private syncTabTitle(): void {
    const n = Math.max(0, Number(this.notifUnread || 0));
    if (!n) {
      this.title.setTitle(this.baseTabTitle);
      return;
    }
    this.title.setTitle(`(${n}) ${this.baseTabTitle}`);
  }
  logout(): void {
    void (async () => {
      this.userNotificationStream.disconnect();
      await this.userNotificationPushService.unsubscribeFromServer();
      this.authService.logout().subscribe(() => {});
      this.storage.signOut();
      this.store.setCurrentUser(null);
      this.title.setTitle(this.baseTabTitle);
      this.router.navigate(['/login']);
    })();
  }

  changeLanguage(lang: string) {
    this.destroyAndReload();
    this.appConfigService.changeLanguage(lang);
  }

  translateFn = (key: string) => {
    if (key) {
      return this.translate.instant(key)
    } else {
      return "";
    }
  }
  isVisible$ = new BehaviorSubject(true);

  destroyAndReload() {
    this.isVisible$.next(false);
    setTimeout(() => {
      this.isVisible$.next(true);
    }, 1);
  }
  isOpen(item : NavigationItem): boolean {
    if(item.children){
      let currentUrl = this.router.url;
      for(let sub of item.children){
        if(sub.link && currentUrl.startsWith(sub.link)){
          return true;
        }
      }
    }
    return false;
  }
}
