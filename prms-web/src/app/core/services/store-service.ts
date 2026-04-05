import { Injectable } from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import {BaseService} from './base-service';

@Injectable({
  providedIn: 'root',
})
export class StoreService {
  readonly URL_API = "/api/v1/users/current"
  private currentUser = new BehaviorSubject<any>(null);
  public setCurrentUser(value: any) {
    this.currentUser.next(value);
  }
  public getCurrentUser() {
    return this.currentUser.asObservable();
  }

  constructor(private baseService: BaseService) { }
  getLoginUser(){
    return this.baseService.get(this.URL_API);
  }
}
