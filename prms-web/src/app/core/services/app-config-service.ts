import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {firstValueFrom} from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AppConfigService {
  private config: any;

  constructor(private http: HttpClient) {}

  async loadConfig() {
    this.config = await firstValueFrom(
      this.http.get('/config/app-config.json')
    );
  }

  get apiUrl() {
    return this.config.apiUrl;
  }
}
