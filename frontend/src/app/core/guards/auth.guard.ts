import { inject } from '@angular/core';
import { Router, CanActivateFn, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable, of } from 'rxjs';
import { switchMap, catchError } from 'rxjs/operators';
import { AuthenticationService } from '../services/authentication.service';

export const authGuard: CanActivateFn = (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot
): Observable<boolean> | boolean => {
  const authService = inject(AuthenticationService);
  const router = inject(Router);

  // Check if we have a valid API authorization token
  const token = authService.authorizationToken;
  if (token && !authService.isTokenExpired(token)) {
    return true;
  }

  // If no valid API token, try to refresh it
  // Note: Azure AD tokens are handled automatically by angular-auth-oidc-client
  const rtoken = authService.getRefreshToken();
  if (rtoken && !authService.isTokenExpired(rtoken)) {
    return authService.refreshAccessToken().pipe(
      switchMap((success) => {
        if (success) {
          return of(true);
        }
        router.navigate(['/auth/login'], { queryParams: { returnUrl: state.url } });
        return of(false);
      }),
      catchError(() => {
        router.navigate(['/auth/login'], { queryParams: { returnUrl: state.url } });
        return of(false);
      })
    );
  } else {
    router.navigate(['/auth/login'], { queryParams: { returnUrl: state.url } });
  }
  return false;
};
