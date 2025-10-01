import { Component, OnInit, OnChanges, Input, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup } from "@angular/forms";
import { TranslateService } from "@ngx-translate/core";
import { MatTableDataSource } from "@angular/material/table";

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
  templateUrl: './audit.component.html',
  styleUrls: ['./audit.component.scss']
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
  tableDataSource = new MatTableDataSource<IAudit>([]);
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
