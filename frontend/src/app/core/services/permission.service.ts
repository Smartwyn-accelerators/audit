import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class PermissionService {
  private permissions: string[] = [];

  constructor() {
    this.loadPermissions();
  }

  private loadPermissions(): void {
    const storedPermissions = localStorage.getItem('permissions');
    this.permissions = storedPermissions ? JSON.parse(storedPermissions) : [];
  }

  hasPermission(permission: string): boolean {
    return this.permissions.includes(permission);
  }

  refreshPermissions(): void {
    this.loadPermissions();
  }

  getPermissions(): string[] {
    return this.permissions;
  }

  hasAnyPermissionForCrud(crudName: string): boolean {
    const requiredPermissions = [
      `${crudName}_CREATE`,
      `${crudName}_UPDATE`,
      `${crudName}_DELETE`,
      `${crudName}_READ`
    ];
    return requiredPermissions.some(permission => this.hasPermission(permission));
  }

  hasAllPermissionForCrud(crudName: string): boolean {
    const requiredPermissions = [
      `${crudName}_CREATE`,
      `${crudName}_UPDATE`,
      `${crudName}_DELETE`,
      `${crudName}_READ`
    ];
    return requiredPermissions.every(permission => this.hasPermission(permission));
  }
}
