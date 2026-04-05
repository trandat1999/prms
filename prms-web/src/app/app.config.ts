import {
  ApplicationConfig, importProvidersFrom,
  inject,
  LOCALE_ID,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection
} from '@angular/core';
import {provideRouter} from '@angular/router';

import {routes} from './app.routes';
import {icons} from './icons-provider';
import {provideNzIcons} from 'ng-zorro-antd/icon';
import {en_US, NZ_I18N, provideNzI18n, vi_VN} from 'ng-zorro-antd/i18n';
import {registerLocaleData} from '@angular/common';
import vi from '@angular/common/locales/vi';
import {provideAnimationsAsync} from '@angular/platform-browser/animations/async';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {AppConfigService} from './core/services/app-config-service';
import {provideTranslateHttpLoader} from '@ngx-translate/http-loader';
import {NzConfig, provideNzConfig} from 'ng-zorro-antd/core/config';
import {provideTranslateService, TranslateService} from '@ngx-translate/core';
import {StorageService} from './core/services/storage-service';
import {NgxSpinnerModule} from 'ngx-spinner';
import {appInterceptor} from './core/guards/app-interceptor';

registerLocaleData(vi);
const ngZorroConfig: NzConfig = {
  message: {
    nzDuration: 5000,
  }
};

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({eventCoalescing: true}),
    provideRouter(routes),
    provideNzIcons(icons),
    provideNzI18n(vi_VN),
    provideAnimationsAsync(),
    provideHttpClient(withInterceptors([appInterceptor])),
    provideAppInitializer(() => {
      const translate = inject(TranslateService);
      const storageService = inject(StorageService);
      translate.use(storageService.getLanguage() || translate.getBrowserLang() || "en");
      const configService = inject(AppConfigService);
      return configService.loadConfig();
    }),
    provideTranslateService({
      fallbackLang: 'en',
      loader: provideTranslateHttpLoader({
        prefix: 'assets/i18n/',
        suffix: '.json',
      })
    }),
    provideNzConfig(ngZorroConfig),
    {
      provide: NZ_I18N, useFactory: () => {
        const localId = inject(LOCALE_ID);
        switch (localId) {
          case 'en':
            return en_US;
          case 'vi':
            return vi_VN;
          default:
            return vi_VN;
        }
      }
    },
    importProvidersFrom([
      NgxSpinnerModule.forRoot({
        type: "ball-spin-clockwise"
      })
    ])
  ]
};
