import { Injectable } from '@angular/core';
import { BaseService } from '../../../../core/services/base-service';

@Injectable({
  providedIn: 'root',
})
export class RoleService {
  private readonly apiUrl = '/api/v1/roles';

  constructor(private base: BaseService) {}

  getAll() {
    return this.base.get(this.apiUrl);
  }
}
