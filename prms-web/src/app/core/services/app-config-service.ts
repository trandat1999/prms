import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {firstValueFrom} from 'rxjs';
import {TranslateService} from '@ngx-translate/core';
import {en_US, NzI18nService, vi_VN} from 'ng-zorro-antd/i18n';
import {StorageService} from './storage-service';

@Injectable({
  providedIn: 'root',
})
export class AppConfigService {
  private config: any;

  constructor(private http: HttpClient,
              private translate: TranslateService,
              private nzI18nService: NzI18nService,
              private localStorage : StorageService) {
    this.changeLanguage(this.localStorage.getLanguage());
  }

  async loadConfig() {
    this.config = await firstValueFrom(
      this.http.get('/assets/config/app-config.json')
    );
  }

  get apiUrl() {
    return this.config.apiUrl;
  }

  changeLanguage(language: string){
    this.localStorage.setLanguage(language);
    this.translate.use(language);
    switch (language) {
      case 'en':
        this.nzI18nService.setLocale(en_US);
        break;
      case 'vi':
        this.nzI18nService.setLocale(vi_VN);
        break;
      default:
        this.nzI18nService.setLocale(en_US);
        break;
    }
  }
}
