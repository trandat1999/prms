import { Component } from '@angular/core';
import {AuthService} from '../../../pages/auth/auth-service';
import {Router, RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {SignalService} from '../../../core/services/signal-service';
import {StoreService} from '../../../core/services/store-service';
import {StorageService} from '../../../core/services/storage-service';
import {AppConfigService} from '../../../core/services/app-config-service';
import {BehaviorSubject} from 'rxjs';
import {navigation, NavigationItem} from '../../utils/navigation.model';
import {NzContentComponent, NzHeaderComponent, NzLayoutComponent, NzSiderComponent} from 'ng-zorro-antd/layout';
import {NzMenuDirective, NzMenuItemComponent, NzSubMenuComponent} from 'ng-zorro-antd/menu';
import {NzIconDirective} from 'ng-zorro-antd/icon';
import {NzOptionComponent, NzSelectComponent} from 'ng-zorro-antd/select';
import {FormsModule} from '@angular/forms';
import {NzAvatarComponent} from 'ng-zorro-antd/avatar';
import {NzDropDownDirective, NzDropdownMenuComponent} from 'ng-zorro-antd/dropdown';
import {Breadcrumb} from '../../breadcrumb/breadcrumb';
import {AsyncPipe, NgOptimizedImage} from '@angular/common';

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
  isCollapsed = false;
  currentLanguage = "en";
  currentUser: any;
  navigation = navigation;
  constructor(private authService: AuthService,
              private router: Router,
              private translate: TranslateService,
              private signalService: SignalService,
              private appConfigService: AppConfigService,
              private store: StoreService,
              private storage: StorageService) {
    this.currentLanguage = this.storage.getLanguage();
    this.store.getCurrentUser().subscribe(user => {
      this.currentUser = user;
    });
    this.signalService.subscribeToSignal().subscribe(signal => {
      if(signal.type === 'navCollapsed'){
        this.isCollapsed = signal.value || false;
      }
    });
  }
  logout(): void {
    this.authService.logout().subscribe(data => {
    });
    this.storage.signOut();
    this.router.navigate(['/login']);
    this.store.setCurrentUser(null);
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
