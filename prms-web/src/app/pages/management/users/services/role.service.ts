import { Injectable } from '@angular/core';
import { map } from 'rxjs';
import { BaseService } from '../../../../core/services/base-service';
import { ApiResponse } from '../../../../shared/utils/api-response';
import { Role } from '../models/role.model';

@Injectable({
  providedIn: 'root',
})
export class RoleService {
  private readonly apiUrl = '/api/v1/roles';

  constructor(private base: BaseService) {}

  getAll() {
    return this.base.get(this.apiUrl).pipe(
      map((res: ApiResponse) => ({
        raw: res,
        roles: (res?.body ?? []) as Role[],
      }))
    );
  }
}

