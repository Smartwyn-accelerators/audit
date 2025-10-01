import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { TimesheetLayoutComponent } from '../../layout/timesheet-layout.component';

@Component({
  selector: 'app-executing-jobs',
  standalone: true,
  imports: [CommonModule, MatCardModule, TimesheetLayoutComponent],
  template: `
    <app-timesheet-layout>
      <div class="executing-jobs-container">
        <mat-card>
          <mat-card-header>
            <mat-card-title>Executing Jobs</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <p>Executing jobs functionality will be implemented here.</p>
          </mat-card-content>
        </mat-card>
      </div>
    </app-timesheet-layout>
  `,
  styles: [`
    .executing-jobs-container {
      padding: 20px;
    }
  `]
})
export class ExecutingJobsComponent {}
