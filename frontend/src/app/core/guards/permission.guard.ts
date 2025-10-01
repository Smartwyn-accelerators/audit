import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { PermissionService } from '../services/permission.service';

@Injectable()
export class PermissionGuard implements CanActivate {
  constructor(
    private router: Router,
    private permissionService: PermissionService
  ) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    // Extract the required permissions from the route data
    const requiredPermissions: string[] = route.data['permissions'] || [];
    const requireAll: boolean = route.data['requireAll'] || false;

    if (requiredPermissions.length === 0) {
      // If no permissions are specified, allow access by default
      return true;
    }

    let hasAccess = false;

    if (requireAll) {
      // Check if the user has all the required permissions
      hasAccess = requiredPermissions.every((permission) =>
        this.permissionService.hasPermission(permission)
      );
    } else {
      // Check if the user has any of the required permissions
      hasAccess = requiredPermissions.some((permission) =>
        this.permissionService.hasPermission(permission)
      );
    }

    if (!hasAccess) {
      // Redirect the user to a not-authorized page or login page
      this.router.navigate(['/not-authorized'], {
        queryParams: { returnUrl: state.url },
      });
      return false;
    }

    return true;
  }
}
