import {
  HttpClient,
  HttpErrorResponse,
  HttpEvent,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest
} from '@angular/common/http';
import {BehaviorSubject, catchError, filter, Observable, switchMap, take, throwError} from 'rxjs';
import {inject} from '@angular/core';
import {StorageService} from '../services/storage-service';
import {Router} from '@angular/router';
import {AppConfigService} from '../services/app-config-service';
import {ApiResponse} from '../../shared/utils/api-response';
import {API_CODE_SUCCESS} from '../../shared/utils/const';

let isRefreshing = false;
const refreshTokenSubject: BehaviorSubject<any> = new BehaviorSubject<any>(null);
export const appInterceptor: HttpInterceptorFn = (request, next) => {
  const storageService = inject(StorageService);
  const http = inject(HttpClient);
  const router = inject(Router);
  if (storageService.getLanguage()) {
    request = request.clone({
      setHeaders: { 'Accept-language': storageService.getLanguage() }
    });
  }
  const token = storageService.getToken();
  if (token) {
    request = request.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  return next(request).pipe(
    catchError((error) => {
      if (error instanceof HttpErrorResponse && error.status === 401) {
        return handle401Error(request, next, http, storageService, router);
      }
      return throwError(() => error);
    })
  );
};

function handle401Error(
  request: HttpRequest<any>,
  next: HttpHandlerFn,
  http: HttpClient,
  storageService: StorageService,
  router: Router
): Observable<HttpEvent<any>> {
  const refreshToken = storageService.getRefreshToken();
  const appConfig = inject(AppConfigService)
  if (!isRefreshing) {
    isRefreshing = true;
    refreshTokenSubject.next(null);

    if (refreshToken) {
      return http
        .get<ApiResponse>(`${appConfig.apiUrl}/api/v1/auth/refresh-token/${refreshToken}`)
        .pipe(
          switchMap((response) => {
            isRefreshing = false;
            if (response.code === API_CODE_SUCCESS) {
              const authResponse = response.body as any;
              storageService.saveToken(authResponse);
              refreshTokenSubject.next(authResponse.accessToken);

              const cloned = request.clone({
                setHeaders: {
                  Authorization: `Bearer ${authResponse.accessToken}`
                }
              });
              return next(cloned);
            } else {
              storageService.signOut();
              router.navigate(['/login']);
              return throwError(() => new Error('Refresh token failed'));
            }
          }),
          catchError((err) => {
            isRefreshing = false;
            storageService.signOut();
            router.navigate(['/login']);
            return throwError(() => err);
          })
        );
    } else {
      storageService.signOut();
      router.navigate(['/login']);
      return throwError(() => new Error('No refresh token'));
    }
  } else {
    return refreshTokenSubject.pipe(
      filter(token => token != null),
      take(1),
      switchMap((newToken) => {
        refreshTokenSubject.next(null);
        const cloned = request.clone({
          setHeaders: {
            Authorization: `Bearer ${newToken}`
          }
        });
        return next(cloned);
      })
    );
  }
}

