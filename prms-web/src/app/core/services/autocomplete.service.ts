import { Injectable } from '@angular/core';
import { BaseService } from './base-service';

export type UserAutocompleteItem = {
  id: string;
  username?: string;
  email?: string;
  fullName?: string;
};

export type SkillAutocompleteItem = {
  id: string;
  code?: string;
  name?: string;
  category?: string;
};

@Injectable({
  providedIn: 'root',
})
export class AutocompleteService {
  private readonly usersUrl = '/api/v1/autocomplete/users';
  private readonly skillsUrl = '/api/v1/autocomplete/skills';

  constructor(private base: BaseService) {}

  searchUsers(keyword: string | null | undefined, pageIndex = 0, pageSize = 20) {
    return this.base.post(this.usersUrl, {
      keyword: keyword?.trim() ? keyword.trim() : null,
      pageIndex,
      pageSize,
      voided: false,
    });
  }

  searchSkills(keyword: string | null | undefined, pageIndex = 0, pageSize = 20) {
    return this.base.post(this.skillsUrl, {
      keyword: keyword?.trim() ? keyword.trim() : null,
      pageIndex,
      pageSize,
      voided: false,
    });
  }
}
