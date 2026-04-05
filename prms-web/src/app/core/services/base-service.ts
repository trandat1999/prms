import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {TranslateService} from '@ngx-translate/core';
import {NgxSpinnerService} from 'ngx-spinner';
import {NzNotificationService} from 'ng-zorro-antd/notification';
import {catchError, map, of} from 'rxjs';
import {ApiResponse} from '../../shared/utils/api-response';
import {AppConfigService} from './app-config-service';

@Injectable({
  providedIn: 'root',
})
export class BaseService {
  private serverUrl: string;
  constructor(
    private http: HttpClient,
    private translate : TranslateService,
    private loading: NgxSpinnerService,
    private notification : NzNotificationService,
    private appConfigService: AppConfigService
  ) {
    this.serverUrl = this.appConfigService.apiUrl;
  }

  get(url:string){
    this.loading.show();
    return this.http.get(this.serverUrl+url).pipe(
      map(value => {
        this.loading.hide();
        return value as ApiResponse;
      }),catchError(error => {
        this.loading.hide();
        this.notification.error(this.translate.instant("common.error") +(error?.error?.code? " "+error?.error?.code:""),
          error?.error?.message? error?.error?.message : this.translate.instant("common.commonError"));
        return of(error)
      })
    );
  }
  delete(url: string){
    this.loading.show();
    return this.http.delete(this.serverUrl+url).pipe(
      map(value => {
        this.loading.hide();
        return value as ApiResponse;
      }),catchError(error => {
        this.loading.hide();
        this.notification.error(this.translate.instant("common.error") +(error?.error?.code? " "+error?.error?.code:""),
          error?.error?.message? error?.error?.message : this.translate.instant("common.commonError"));
        return of(error)
      })
    );
  }
  put(url : string,request :any ){
    this.loading.show();
    return this.http.put(this.serverUrl+url, request).pipe(
      map(value => {
        this.loading.hide().then();
        return value as ApiResponse;
      }),catchError(error => {
        this.loading.hide();
        this.notification.error(this.translate.instant("common.error") +(error?.error?.code? " "+error?.error?.code:""),
          error?.error?.message? error?.error?.message : this.translate.instant("common.commonError"));
        return of(error)
      })
    );
  }

  post(url : string,request :any ){
    this.loading.show().then();
    return this.http.post(this.serverUrl+url, request).pipe(
      map(value => {
        this.loading.hide().then();
        return value as ApiResponse;
      }),catchError(error => {
        console.log(error);
        this.loading.hide().then();
        this.notification.error(this.translate.instant("common.error") +(error?.error?.code? " "+error?.error?.code:""),
          error?.error?.message? error?.error?.message : this.translate.instant("common.commonError"));
        return of(error)
      })
    );
  }
}
