import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap } from 'rxjs/operators';
import { throwError, of } from 'rxjs';
import { AuthenticationService } from '../services/authentication.service';
import { CookieService } from '../services/cookie.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthenticationService);
  const cookieService = inject(CookieService);

  return next(req).pipe(
    catchError((err) => {
      if (err.status === 401 || err.status === 403) {
        console.log('Unauthorized request intercepted - attempting API token refresh:', err.message);
        // Only refresh custom API tokens, not Azure AD tokens
        // Azure AD tokens are handled automatically by angular-auth-oidc-client
        return authService.refreshAccessToken().pipe(
          switchMap((success) => {
            if (success) {
              const newAuthToken = authService.authorizationToken;
              const headers = {
                'X-XSRF-TOKEN': cookieService.get('XSRF-TOKEN') || '',
                'Accept': 'application/json',
                'Authorization': newAuthToken || '',
              };
              return next(req.clone({
                withCredentials: true,
                setHeaders: headers,
              }));
            }
            return throwError(() => new Error('API token refresh failed'));
          }),
          catchError((refreshErr) => {
            return throwError(() => refreshErr);
          })
        );
      }

      const error = err?.error?.message || err?.statusText;
      return throwError(() => error);
    })
  );
};
