import { inject } from '@angular/core';
import { CanActivateFn, Router, RouterStateSnapshot } from '@angular/router';
import { AuthenticationService } from '../services/authentication.service';

export const landingGuard: CanActivateFn = (
  route,
  state: RouterStateSnapshot
): boolean => {
  const authService = inject(AuthenticationService);
  const router = inject(Router);

  if (authService.idToken) {
    router.navigate(['/home']);
  } else if (state.url === '/') {
    router.navigate(['/auth/login']);
  }
  return true;
};
