import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, throwError, BehaviorSubject, of } from 'rxjs';
import { catchError, map, switchMap, filter, take } from 'rxjs/operators';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { JwtHelperService } from '@auth0/angular-jwt';

import { ITokenDetail } from '../models/token-details.interface';
import { CookieService } from './cookie.service';
import { PermissionService } from './permission.service';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private permissionService = inject(PermissionService);
  private cookieService = inject(CookieService);
  private jwtHelper = new JwtHelperService();
  private oidcSecurityService = inject(OidcSecurityService);

  private apiUrl = environment.apiUrl;
  private decodedToken: ITokenDetail = { sub: '', id: 0, name: '' };
  private token = '';
  private isRefreshing = false;
  private refreshTokenSubject = new BehaviorSubject<string | null>(null);

  authChange = new BehaviorSubject<string>('');
  permissionsChange = new BehaviorSubject<string>('');

  configure() {
    this.oidcSecurityService.checkAuth().subscribe((authResult: any) => {
      const { isAuthenticated, userData, accessToken, idToken } = authResult;
      if (isAuthenticated && idToken) {
        console.log('User Authorized');
        this.token = idToken;
        this.setLoggedInUserPermissions(idToken);
      } else {
        console.error('Authorization failed');
      }
    });
  }

  login(user?: any, redirectPath?: string) {
    this.oidcSecurityService.authorize();
  }

  logout() {
    console.log('Starting logout process...');
    
    // Clear all local storage
    localStorage.removeItem('permissions');
    localStorage.removeItem('Authorization');
    
    // Clear all cookies
    this.cookieService.delete('Authentication');
    this.cookieService.delete('RefreshToken');
    this.cookieService.delete('XSRF-TOKEN');
    
    // Reset internal state
    this.token = '';
    this.decodedToken = { sub: '', id: 0, name: '' };
    this.isRefreshing = false;
    this.refreshTokenSubject.next(null);
    
    // Notify about auth change
    this.authChange.next('');
    this.permissionsChange.next('');
    
    // Perform OIDC logout with proper error handling
    this.oidcSecurityService.logoff().subscribe({
      next: () => {
        console.log('OIDC logout successful, redirecting to login...');
        this.router.navigate(['/auth/login']);
      },
      error: (error) => {
        console.error('OIDC logout error:', error);
        // Even if OIDC logout fails, redirect to login
        this.router.navigate(['/auth/login']);
      }
    });
    
    // Fallback: if OIDC logout doesn't complete within 3 seconds, force redirect
    setTimeout(() => {
      console.log('Fallback logout redirect...');
      this.router.navigate(['/auth/login']);
    }, 3000);
  }

  getLoggedinUserId(): number | undefined {
    const token = localStorage.getItem('Authorization') || '';
    const decodedToken: ITokenDetail = this.decodePassedToken(token);
    return decodedToken?.id;
  }

  getRefreshToken(): string | null {
    const refreshToken = this.cookieService.get('RefreshToken');
    console.log('Getting RefreshToken from cookie:', refreshToken);
    console.log('All cookies:', document.cookie);
    return refreshToken;
  }

  // Debug method to check all cookies
  debugCookies(): void {
    console.log('=== Cookie Debug Info ===');
    console.log('All cookies:', document.cookie);
    console.log('RefreshToken cookie:', this.cookieService.get('RefreshToken'));
    console.log('Authentication cookie:', this.cookieService.get('Authentication'));
    console.log('XSRF-TOKEN cookie:', this.cookieService.get('XSRF-TOKEN'));
    console.log('========================');
  }

  get authorizationToken(): string | null {
    return localStorage.getItem('Authorization');
  }

  get idToken(): string | null {
    return this.token;
  }

  decodeToken(): ITokenDetail {
    if (this.decodedToken && this.decodedToken.sub) {
      const permissions: string = localStorage.getItem('permissions') || '[]';
      this.decodedToken.scopes = JSON.parse(permissions) || [];
      return this.decodedToken;
    } else if (this.token) {
      const decodedToken: ITokenDetail = this.jwtHelper.decodeToken(this.token) as ITokenDetail;
      const permissions: string = localStorage.getItem('permissions') || '[]';
      decodedToken.scopes = JSON.parse(permissions) || [];
      this.decodedToken = decodedToken;
      return this.decodedToken;
    }
    return { sub: '', id: 0, name: '' };
  }

  decodePassedToken(token: string): ITokenDetail {
    return this.jwtHelper.decodeToken(token) as ITokenDetail;
  }

  setLoggedInUserPermissions(idToken: string) {
    if (idToken) {
      this.token = idToken;
      // Use observe: 'response' to access headers
      this.http.get<{ token: string }>(`${this.apiUrl}/auth/getAuthorizationToken`, { observe: 'response', withCredentials: true }).subscribe({
        next: (response) => {
          console.log('Authorization token received:', response.body?.token);
          console.log('Authorization response headers:', response.headers);
          console.log('All cookies after auth:', document.cookie);

          // Set authorization token and permissions
          if (response.body?.token) {
            localStorage.setItem('Authorization', response.body.token);
            const decodedToken = this.decodePassedToken(response.body.token);
            const permissions = decodedToken ? decodedToken.scopes : [];
            localStorage.setItem('permissions', JSON.stringify(permissions));
            this.permissionService.refreshPermissions();
            this.permissionsChange.next('');
          }

          // The Set-Cookie header is not accessible via JavaScript in browser responses due to security reasons.
          // Browsers do not expose Set-Cookie headers to JavaScript (response.headers.get('Set-Cookie') will always be null).
          // Instead, if the server sets Set-Cookie and withCredentials: true is used, the browser will handle cookies automatically.
          // No need to manually extract or set the RefreshToken cookie here.
          // If you need to access the cookie, use this.cookieService.get('RefreshToken') after the response.
          console.log('RefreshToken', this.getRefreshToken());
          // Handle redirect after permissions are set
          const redirectUrl = sessionStorage.getItem('redirectUrl');
          if (redirectUrl) {
            sessionStorage.removeItem('redirectUrl');
            this.router.navigateByUrl(redirectUrl);
          } else {
            // Default redirect to home after successful authentication
            this.router.navigateByUrl('/home');
          }
        },
        error: this.handleError
      });
    }
  }

  refreshAccessToken(): Observable<boolean> {
    // Only handle custom API token refresh, not Azure AD tokens
    // Azure AD tokens are handled automatically by angular-auth-oidc-client
    if (this.isRefreshing) {
      return this.refreshTokenSubject.pipe(
        filter(token => token !== null),
        take(1),
        switchMap(() => of(true))
      );
    }

    this.isRefreshing = true;
    this.refreshTokenSubject.next(null);

    return this.http.post<{ token: string }>(`${this.apiUrl}/auth/refresh`, {}, { 
      withCredentials: true,
      observe: 'response' as const
    }).pipe(
      map((response) => {
        console.log('Refresh response headers:', response.headers);
        console.log('Refresh response body:', response.body);
        
        // Wait a moment for the browser to set the refresh token cookie
        setTimeout(() => {
          console.log('RefreshToken after refresh:', this.getRefreshToken());
          this.debugCookies();
        }, 100);
        
        const redirectUrl = sessionStorage.getItem('redirectUrl');
        if (redirectUrl) {
          sessionStorage.removeItem('redirectUrl');
          this.router.navigateByUrl(redirectUrl);
        }
        localStorage.setItem('Authorization', response.body?.token || '');
        const decodedToken = this.decodePassedToken(response.body?.token || '');
        const permissions = decodedToken ? decodedToken.scopes : [];
        localStorage.setItem('permissions', JSON.stringify(permissions));
        this.permissionService.refreshPermissions();
        this.permissionsChange.next('');
        this.isRefreshing = false;
        this.refreshTokenSubject.next(response.body?.token || '');
        return true;
      }),
      catchError((err: HttpErrorResponse) => {
        console.error('API token refresh failed', err);
        this.isRefreshing = false;
        this.refreshTokenSubject.next(null);
        if (err.status === 401 || err.status === 403 || err.status === 400) {
          this.logout();
          this.oidcSecurityService.authorize();
        }
        return of(false);
      })
    );
  }

  getTokenExpirationDate(token: string): Date | null {
    const decoded: any = this.jwtHelper.decodeToken(token);
    if (decoded.exp === undefined) {
      return null;
    }
    const date = new Date(0);
    date.setUTCSeconds(decoded.exp);
    return date;
  }

  isTokenExpired(token?: string): boolean {
    if (!token) {
      return true;
    }
    const date: Date | null = this.getTokenExpirationDate(token);
    if (date === null) {
      return false;
    }
    return !(date.valueOf() > new Date().valueOf());
  }

  private handleError = (err: HttpErrorResponse) => {
    let errorMessage = '';
    if (err.error instanceof ErrorEvent) {
      errorMessage = `An error occurred: ${err.error.message}`;
    } else {
      errorMessage = `Server returned code: ${err.status}, error message is: ${err.message}`;
    }
    console.error(errorMessage);
    return throwError(() => err.message);
  };
}
