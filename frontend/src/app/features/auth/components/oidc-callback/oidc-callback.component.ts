import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { AuthenticationService } from '../../../../core/services/authentication.service';

@Component({
  selector: 'app-oidc-callback',
  standalone: true,
  template: `
    <div class="callback-container">
      <div class="spinner">
        <div class="spinner-circle"></div>
        <p>Processing authentication...</p>
      </div>
    </div>
  `,
  styles: [`
    .callback-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }
    .spinner {
      text-align: center;
      color: white;
    }
    .spinner-circle {
      width: 40px;
      height: 40px;
      border: 4px solid rgba(255, 255, 255, 0.3);
      border-top: 4px solid white;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin: 0 auto 16px;
    }
    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
  `]
})
export class OidcCallbackComponent implements OnInit {
  private authService = inject(AuthenticationService);
  private router = inject(Router);
  private oidcSecurityService = inject(OidcSecurityService);

  ngOnInit() {
    this.oidcSecurityService.checkAuth().subscribe({
      next: ({ isAuthenticated, idToken }) => {
        if (isAuthenticated && idToken) {
          console.log('User is authenticated');
          this.authService.setLoggedInUserPermissions(idToken);
        } else {
          console.error('Authentication failed');
          this.router.navigate(['/auth/login']);
        }
      },
      error: (error) => {
        console.error('Authentication error:', error);
        this.router.navigate(['/auth/login']);
      }
    });
  }
}
