import {CanActivateFn, Router} from '@angular/router';
import {inject} from '@angular/core';
import {StorageService} from '../services/storage-service';

export const loginGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const storage = inject(StorageService);
  if(storage.getToken()){
    router.navigate(["/welcome"])
    return false;
  }
  return true;
};
