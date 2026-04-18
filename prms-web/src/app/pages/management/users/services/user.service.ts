import { Injectable } from '@angular/core';
import { map } from 'rxjs';
import { BaseService } from '../../../../core/services/base-service';
import { ApiResponse } from '../../../../shared/utils/api-response';
import { Page } from '../../../project/models/page.model';
import { User, UserCreatePayload, UserDetail, UserUpdatePayload } from '../models/user.model';
import { UserSearchRequest } from '../models/user-search.request';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly apiUrl = '/api/v1/users';

  constructor(private base: BaseService) {}

  getPage(request: UserSearchRequest) {
    return this.base.post(this.apiUrl + '/page', request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        page: (res?.body ?? null) as Page<User> | null,
      }))
    );
  }

  getById(id: string) {
    return this.base.get(`${this.apiUrl}/${id}`).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        user: (res?.body ?? null) as UserDetail | null,
      }))
    );
  }

  create(request: UserCreatePayload) {
    return this.base.post(this.apiUrl, request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        user: (res?.body ?? null) as UserDetail | null,
      }))
    );
  }

  update(id: string, request: UserUpdatePayload) {
    return this.base.put(`${this.apiUrl}/${id}`, request).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        user: (res?.body ?? null) as UserDetail | null,
      }))
    );
  }

  delete(id: string) {
    return this.base.delete(`${this.apiUrl}/${id}`).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        user: (res?.body ?? null) as User | null,
      }))
    );
  }

  updatePassword(id: string, newPassword: string) {
    return this.base.patch(`${this.apiUrl}/${id}/password`, { newPassword }).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        ok: (res?.body ?? null) as boolean | null,
      }))
    );
  }
}

