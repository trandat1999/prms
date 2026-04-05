import {CanActivateFn, Router} from '@angular/router';
import {inject} from '@angular/core';
import {StorageService} from '../services/storage-service';
import {StoreService} from '../services/store-service';
import {catchError, map, of, switchMap} from 'rxjs';
import {ApiResponse} from '../../shared/utils/api-response';
import {API_CODE_SUCCESS} from '../../shared/utils/const';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const storage = inject(StorageService);
  const storeService = inject(StoreService);
  const token = storage.getToken();
  if (!token) {
    router.navigate(['/login']);
    return of(false);
  }
  return storeService.getCurrentUser().pipe(
    switchMap((user) => {
      if (user) {
        return of(true);
      } else {
        return storeService.getLoginUser().pipe(
          map((value: ApiResponse) => {
            if (value.code === API_CODE_SUCCESS) {
              storeService.setCurrentUser(value.body);
              return true;
            } else {
              storage.signOut();
              router.navigate(['/login']);
              return false;
            }
          }),
          catchError(() => {
            storage.signOut();
            router.navigate(['/login']);
            return of(false);
          })
        );
      }
    }),
    catchError(() => {
      storage.signOut();
      router.navigate(['/login']);
      return of(false);
    })
  );
};
