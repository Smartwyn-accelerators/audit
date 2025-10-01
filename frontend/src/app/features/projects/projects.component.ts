import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { TimesheetLayoutComponent } from '../layout/timesheet-layout.component';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [CommonModule, MatCardModule, TimesheetLayoutComponent],
  template: `
    <app-timesheet-layout>
      <div class="projects-container">
        <mat-card>
          <mat-card-header>
            <mat-card-title>Projects Management</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <p>Project management functionality will be implemented here.</p>
          </mat-card-content>
        </mat-card>
      </div>
    </app-timesheet-layout>
  `,
  styles: [`
    .projects-container {
      padding: 20px;
    }
  `]
})
export class ProjectsComponent {}
