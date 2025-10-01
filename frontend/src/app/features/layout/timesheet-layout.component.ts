import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatTreeModule, MatTreeFlatDataSource, MatTreeFlattener } from '@angular/material/tree';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatDividerModule } from '@angular/material/divider';

import { AuthenticationService } from '../../core/services/authentication.service';
import { PermissionService } from '../../core/services/permission.service';
import { UserService, User } from '../../core/services/user.service';
import { PERMISSIONS } from '../../core/models/permission-constant';
import { FlatTreeControl } from '@angular/cdk/tree';

interface MenuNode {
  name: string;
  route: string;
  iconName: string;
  children?: MenuNode[];
}

interface FlatMenuNode {
  name: string;
  route: string;
  iconName: string;
  expandable: boolean;
  level: number;
  disabled: boolean;
  matTreeNodePaddingIndent: number;
  matTreeNodePadding: string;
}

@Component({
  selector: 'app-timesheet-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatSidenavModule,
    MatTreeModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    MatBadgeModule,
    MatDividerModule,
  ],
  templateUrl: './timesheet-layout.component.html',
  styleUrls: ['./timesheet-layout.component.scss']
})
export class TimesheetLayoutComponent implements OnInit, OnDestroy {
  private authService = inject(AuthenticationService);
  private permissionService = inject(PermissionService);
  private userService = inject(UserService);
  private router = inject(Router);

  sidenavData: MenuNode[] = [
    {
      name: "Home",
      route: "/home",
      iconName: "home",
    }
  ];
  selectedNode: any = null;

  treeControl = new FlatTreeControl<FlatMenuNode>(
    (item: any) => item.level,
    (item: any) => item.expandable
  );

  treeFlattener = new MatTreeFlattener(
    this.transformNode,
    (item) => item.level,
    (item) => item.expandable,
    (item) => item.children
  );

  datasource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);
  username = "";
  notifications: Notification[] = [];
  notificationCount = 0;
  subscription: any;

  hasChild = (_: number, item: FlatMenuNode) => item.expandable;
  hasNoContent = (_: number, _item: FlatMenuNode) => _item.name === "";

  ngOnInit(): void {
    this.authService.permissionsChange.subscribe(() => {
      this.initializeData();
    });
  }

  initializeData() {
    this.setUser();
    this.fillSidenavData();
  }



  setUser() {
    const userid = this.authService.getLoggedinUserId();
    if (userid) {
      this.userService.getUser(userid).subscribe((user) => { 
        this.username = user.emailAddress; 
      });
    }
  }

  fillSidenavData() {
    this.addPermissionsBasedData();
    this.addSchedulerData();
  }

  addSchedulerData() {
    const schedulerData: MenuNode[] = [];
    
    if (this.permissionService.hasAnyPermissionForCrud("JOBDETAILSENTITY")) {
      schedulerData.push({
        name: "Jobs",
        route: "/timesheet/scheduler/jobs",
        iconName: "work",
      });
      schedulerData.push({
        name: "Executing Jobs",
        route: "/timesheet/scheduler/executingJobs",
        iconName: "offline_bolt",
      });
      schedulerData.push({
        name: "Execution History",
        route: "/timesheet/scheduler/executionHistory",
        iconName: "history",
      });
    }
    
    if (this.permissionService.hasAllPermissionForCrud("TRIGGERDETAILSENTITY")) {
      schedulerData.push({
        name: "Triggers",
        route: "/timesheet/scheduler/triggers",
        iconName: "control_point_duplicate",
      });
    }
    
    this.sidenavData = this.sidenavData.concat(schedulerData);
    this.datasource.data = this.sidenavData;
  }



  resetNotificationView() {
    this.notificationCount = 0;
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  onNodeSelect(node: any): void {
    this.selectedNode = node;
  }

  logout() {
    this.authService.logout();
  }

  private transformNode(item: MenuNode, level: number): FlatMenuNode {
    return {
      name: item.name,
      iconName: item.iconName,
      route: item.route,
      expandable: !!item.children,
      disabled: false,
      matTreeNodePaddingIndent: level > 1 ? level - 1 : 1,
      matTreeNodePadding: "0px",
      level: level,
    };
  }

  private addPermissionsBasedData(): void {
    this.sidenavData = this.getBasePermissions();
    this.datasource.data = this.sidenavData;
  }

  private getBasePermissions(): MenuNode[] {
    const data: MenuNode[] = [];

    if (this.permissionService.hasAnyPermissionForCrud("CUSTOMER")) {
      data.push({
        name: "Customers",
        route: "/timesheet/customers",
        iconName: "attribution",
      });
    }
    
    if (!this.permissionService.hasAnyPermissionForCrud("CUSTOMER") && 
        this.permissionService.hasPermission(PERMISSIONS.PROJECT.READ)) {
      data.push({
        name: "Projects",
        route: "/timesheet/projects",
        iconName: "area_chart",
      });
    }
    
    if (this.permissionService.hasPermission(PERMISSIONS.TIMESHEET.REVIEW) ||
        this.permissionService.hasAnyPermissionForCrud("TIMESHEET")) {
      data.push({
        name: "Timesheet Review",
        route: "/timesheet/overview",
        iconName: "work_history",
      });
    }
    
    if (this.permissionService.hasPermission(PERMISSIONS.TIMESHEET.SUBMIT) ||
        this.permissionService.hasAnyPermissionForCrud("TIMESHEET_DETAIL")) {
      this.replaceOrAdd(data, {
        name: "Timesheet Management",
        route: "/timesheet/details",
        iconName: "work_history",
      });
    }

    if (this.permissionService.hasAllPermissionForCrud("TIMEOFF")) {
      data.push({
        name: "Timesheet Time Off Management",
        route: "/timesheet/timeOff",
        iconName: "work_off"
      });
    }
    
    if (!this.permissionService.hasAnyPermissionForCrud("AUDIT")) {
      data.push({
        name: "Audit",
        route: "/timesheet/audit",
        iconName: "fact_check",
      });
    }

    return data;
  }

  private replaceOrAdd(data: MenuNode[], newItem: MenuNode): void {
    const index = data.findIndex((item) => item.name === "Timesheet Review");
    if (index >= 0) {
      data.splice(index, 1);
    }
    data.push(newItem);
  }
}
