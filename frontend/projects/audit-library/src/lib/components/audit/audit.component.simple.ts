import { Component, OnInit, OnChanges, Input, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup } from "@angular/forms";
import { TranslateService } from "@ngx-translate/core";

// Interfaces
export interface IAudit {
  action: string;
  actor: string;
  origin: string;
  timestamp: string;
  elements: Array<{name: string, value: string}>;
  [key: string]: any;
}

export interface AuditFilterConfig {
  showUserFilter?: boolean;
  showDateFilter?: boolean;
  showApiFilter?: boolean;
  showOperationFilter?: boolean;
  showEntityFilter?: boolean;
}

@Component({
  selector: 'mal-audit',
  template: `
    <div class="mal-audit-container">
      <div class="mal-header">
        <h1 class="mal-title">{{title}}</h1>
      </div>

      <div class="mal-card">
        <form [formGroup]="filterForm" class="mal-filter-form">
          <div class="mal-filter-grid">
            <div class="mal-filter-item" *ngIf="filterConfig.showDateFilter">
              <label>From Date:</label>
              <input formControlName="from" type="date">
            </div>
            <div class="mal-filter-item" *ngIf="filterConfig.showDateFilter">
              <label>To Date:</label>
              <input formControlName="to" type="date">
            </div>
            <div class="mal-filter-item" *ngIf="filterConfig.showUserFilter">
              <label>User:</label>
              <input formControlName="user" type="text" placeholder="Enter user">
            </div>
            <div class="mal-filter-item" *ngIf="filterConfig.showApiFilter">
              <label>API Path:</label>
              <input formControlName="apiPath" type="text" placeholder="Enter API path">
            </div>
            <div class="mal-filter-item" *ngIf="filterConfig.showOperationFilter">
              <label>Operation:</label>
              <input formControlName="operation" type="text" placeholder="Enter operation">
            </div>
            <div class="mal-filter-item" *ngIf="filterConfig.showEntityFilter">
              <label>Entity:</label>
              <select formControlName="entity">
                <option value="">All</option>
                <option *ngFor="let entity of entityList" [value]="entity">{{entity}}</option>
              </select>
            </div>
          </div>
          
          <div class="mal-filter-actions">
            <button type="button" (click)="onFilterSubmit()" class="mal-button mal-button-primary">
              Search
            </button>
            <button type="button" (click)="onClearFilters()" class="mal-button mal-button-secondary">
              Clear
            </button>
          </div>
        </form>
      </div>

      <div class="mal-table-container" *ngIf="!isLoading">
        <table class="mal-table">
          <thead>
            <tr>
              <th>Action</th>
              <th>Actor</th>
              <th>Origin</th>
              <th>Operation</th>
              <th>Entity</th>
              <th>Navigated To</th>
              <th>API Path</th>
              <th>Timestamp</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let auditEntry of tableDataSource.data" (click)="onRowClick(auditEntry)" class="mal-table-row">
              <td>{{auditEntry.action}}</td>
              <td>{{auditEntry.actor}}</td>
              <td>{{auditEntry.origin}}</td>
              <td>{{getElementValue(auditEntry.elements, 'operation')}}</td>
              <td>{{getElementValue(auditEntry.elements, 'entityName')}}</td>
              <td>{{auditEntry.navigatedTo}}</td>
              <td>{{auditEntry.APIPath}}</td>
              <td>{{auditEntry.timestamp}}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="mal-loading" *ngIf="isLoading">
        <div class="mal-spinner"></div>
        <p>Loading...</p>
      </div>
    </div>
  `,
  styles: [`
    .mal-audit-container {
      padding: 20px;
      font-family: Arial, sans-serif;
    }
    
    .mal-header {
      margin-bottom: 20px;
    }
    
    .mal-title {
      color: #1976d2;
      margin: 0;
    }
    
    .mal-card {
      background: white;
      border: 1px solid #ddd;
      border-radius: 8px;
      padding: 20px;
      margin-bottom: 20px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    
    .mal-filter-form {
      display: flex;
      flex-direction: column;
      gap: 15px;
    }
    
    .mal-filter-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 15px;
    }
    
    .mal-filter-item {
      display: flex;
      flex-direction: column;
      gap: 5px;
    }
    
    .mal-filter-item label {
      font-weight: bold;
      color: #333;
    }
    
    .mal-filter-item input,
    .mal-filter-item select {
      padding: 8px;
      border: 1px solid #ccc;
      border-radius: 4px;
    }
    
    .mal-filter-actions {
      display: flex;
      gap: 10px;
      margin-top: 10px;
    }
    
    .mal-button {
      padding: 10px 20px;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-weight: bold;
    }
    
    .mal-button-primary {
      background: #1976d2;
      color: white;
    }
    
    .mal-button-secondary {
      background: #f5f5f5;
      color: #333;
      border: 1px solid #ccc;
    }
    
    .mal-table-container {
      overflow-x: auto;
    }
    
    .mal-table {
      width: 100%;
      border-collapse: collapse;
      background: white;
      border: 1px solid #ddd;
      border-radius: 8px;
      overflow: hidden;
    }
    
    .mal-table th {
      background: #f5f5f5;
      padding: 12px;
      text-align: left;
      font-weight: bold;
      border-bottom: 1px solid #ddd;
    }
    
    .mal-table td {
      padding: 12px;
      border-bottom: 1px solid #eee;
    }
    
    .mal-table-row {
      cursor: pointer;
    }
    
    .mal-table-row:hover {
      background: #f9f9f9;
    }
    
    .mal-loading {
      text-align: center;
      padding: 40px;
    }
    
    .mal-spinner {
      width: 40px;
      height: 40px;
      border: 4px solid #f3f3f3;
      border-top: 4px solid #1976d2;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin: 0 auto 20px;
    }
    
    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
  `]
})
export class AuditComponent implements OnInit, OnChanges {
  @Input() dataSource: IAudit[] = [];
  @Input() filterConfig: AuditFilterConfig = {
    showUserFilter: true,
    showDateFilter: true,
    showApiFilter: true,
    showOperationFilter: true,
    showEntityFilter: true
  };
  @Input() entityList: string[] = [];
  @Input() isLoading: boolean = false;
  @Input() title: string = 'Audit Trail';

  @Output() filterApplied = new EventEmitter<any>();
  @Output() clearFilters = new EventEmitter<void>();
  @Output() rowClicked = new EventEmitter<IAudit>();

  filterForm: FormGroup;
  tableDataSource: { data: IAudit[] } = { data: [] };
  displayedColumns: string[] = ['action', 'actor', 'origin', 'operation', 'entityName', 'navigatedTo', 'APIPath', 'timestamp'];

  constructor(
    private fb: FormBuilder,
    private translate: TranslateService
  ) {
    this.filterForm = this.fb.group({
      from: [''],
      to: [''],
      user: [''],
      apiPath: [''],
      operation: [''],
      entity: ['']
    });
  }

  ngOnInit() {
    this.tableDataSource.data = this.dataSource;
  }

  ngOnChanges() {
    if (this.dataSource) {
      this.tableDataSource.data = this.dataSource;
    }
  }

  onFilterSubmit() {
    const filterValue = this.filterForm.value;
    this.filterApplied.emit(filterValue);
  }

  onClearFilters() {
    this.filterForm.reset();
    this.clearFilters.emit();
  }

  onRowClick(row: IAudit) {
    this.rowClicked.emit(row);
  }

  getElementValue(elements: Array<{name: string, value: string}>, name: string): string {
    const element = elements.find(el => el.name === name);
    return element ? element.value : '';
  }
}
