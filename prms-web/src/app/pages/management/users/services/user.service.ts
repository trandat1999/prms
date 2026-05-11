import { Injectable } from '@angular/core';
import { BaseService } from '../../../../core/services/base-service';
import {
  CurrentUserProfilePayload,
  UserCreatePayload,
  UserUpdatePayload,
} from '../models/user.model';
import { UserSearchRequest } from '../models/user-search.request';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly apiUrl = '/api/v1/users';

  constructor(private base: BaseService) {}

  getCurrent() {
    return this.base.get(`${this.apiUrl}/current`);
  }

  updateCurrentProfile(request: CurrentUserProfilePayload) {
    return this.base.patch(`${this.apiUrl}/current/profile`, request);
  }

  updateCurrentPassword(currentPassword: string, newPassword: string) {
    return this.base.patch(`${this.apiUrl}/current/password`, { currentPassword, newPassword });
  }

  getPage(request: UserSearchRequest) {
    return this.base.post(this.apiUrl + '/page', request);
  }

  getById(id: string) {
    return this.base.get(`${this.apiUrl}/${id}`);
  }

  create(request: UserCreatePayload) {
    return this.base.post(this.apiUrl, request);
  }

  update(id: string, request: UserUpdatePayload) {
    return this.base.put(`${this.apiUrl}/${id}`, request);
  }

  delete(id: string) {
    return this.base.delete(`${this.apiUrl}/${id}`);
  }

  updatePassword(id: string, newPassword: string) {
    return this.base.patch(`${this.apiUrl}/${id}/password`, { newPassword });
  }

  getSkills(id: string) {
    return this.base.get(`${this.apiUrl}/${id}/skills`);
  }

  updateSkills(id: string, items: any[]) {
    return this.base.put(`${this.apiUrl}/${id}/skills`, { items });
  }
}
