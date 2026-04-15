import { Injectable } from '@angular/core';
import { map } from 'rxjs';
import { BaseService } from './base-service';
import { ApiResponse } from '../../shared/utils/api-response';

export type UserAutocompleteItem = {
  id: string;
  username?: string;
  email?: string;
  fullName?: string;
};

@Injectable({
  providedIn: 'root',
})
export class AutocompleteService {
  private readonly usersUrl = '/api/v1/autocomplete/users';

  constructor(private base: BaseService) {}

  searchUsers(keyword: string | null | undefined, pageIndex = 0, pageSize = 20) {
    return this.base.post(this.usersUrl, {
      keyword: keyword?.trim() ? keyword.trim() : null,
      pageIndex,
      pageSize,
      voided: false,
    }).pipe(
      map((res: ApiResponse) => {
        const b = res.body;
        if (Array.isArray(b)) {
          return b as UserAutocompleteItem[];
        }
        return ((b as { content?: UserAutocompleteItem[] })?.content ?? []) as UserAutocompleteItem[];
      })
    );
  }
}
