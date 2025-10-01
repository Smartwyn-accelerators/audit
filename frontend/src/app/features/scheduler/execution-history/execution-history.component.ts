import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { TimesheetLayoutComponent } from '../../layout/timesheet-layout.component';

@Component({
  selector: 'app-execution-history',
  standalone: true,
  imports: [CommonModule, MatCardModule, TimesheetLayoutComponent],
  template: `
    <app-timesheet-layout>
      <div class="execution-history-container">
        <mat-card>
          <mat-card-header>
            <mat-card-title>Execution History</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <p>Execution history functionality will be implemented here.</p>
          </mat-card-content>
        </mat-card>
      </div>
    </app-timesheet-layout>
  `,
  styles: [`
    .execution-history-container {
      padding: 20px;
    }
  `]
})
export class ExecutionHistoryComponent {}
