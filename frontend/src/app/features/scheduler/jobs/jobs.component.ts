import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { TimesheetLayoutComponent } from '../../layout/timesheet-layout.component';

@Component({
  selector: 'app-jobs',
  standalone: true,
  imports: [CommonModule, MatCardModule, TimesheetLayoutComponent],
  template: `
    <app-timesheet-layout>
      <div class="jobs-container">
        <mat-card>
          <mat-card-header>
            <mat-card-title>Jobs Management</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <p>Jobs management functionality will be implemented here.</p>
          </mat-card-content>
        </mat-card>
      </div>
    </app-timesheet-layout>
  `,
  styles: [`
    .jobs-container {
      padding: 20px;
    }
  `]
})
export class JobsComponent {}
