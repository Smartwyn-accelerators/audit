import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthenticationService } from '../services/authentication.service';
import { CookieService } from '../services/cookie.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthenticationService);
  const cookieService = inject(CookieService);

  // Skip OIDC provider URLs
  if (req.url.includes('login.microsoftonline.com')) {
    return next(req);
  }

  let headers: Record<string, string> = {
    'X-XSRF-TOKEN': cookieService.get('XSRF-TOKEN') || '',
    'Accept': 'application/json'
  };

  const idToken = authService.idToken;
  if (idToken && req.url.includes('/auth/getAuthorizationToken')) {
    cookieService.set('Authentication', 'Bearer_' + idToken);
  } else {
    cookieService.delete('Authentication');
  }

  if (authService.authorizationToken) {
    headers['Authorization'] = authService.authorizationToken;
  }

  // Note: RefreshToken is handled by the server via Set-Cookie header
  // We don't need to manually set it here as it's automatically handled by the browser
  // when withCredentials: true is used

  const modifiedReq = req.clone({
    withCredentials: true,
    setHeaders: headers
  });

  return next(modifiedReq);
};
