import { Component, OnInit, OnChanges, OnDestroy, Input, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { HttpClient } from '@angular/common/http';

// Types
import { IAudit, AuditFilterConfig } from '../../types/audit.types';

@Component({
  selector: 'mal-audit',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TranslateModule
  ],
  template: `
    <div class="mal-audit-container">
      <div class="mal-header">
        <h1 class="mal-title">{{title}}</h1>
      </div>

      <div class="mal-card">
        <form [formGroup]="filterForm" class="mal-filter-form">
          <div class="mal-filter-grid">
            <div class="mal-filter-item" *ngIf="filterConfig.showDateFilter">
              <label class="mal-label">{{'AUDIT.FILTERS.START_DATE' | translate}}</label>
              <input formControlName="from" type="date" class="mal-input">
            </div>
            
            <div class="mal-filter-item" *ngIf="filterConfig.showDateFilter">
              <label class="mal-label">{{'AUDIT.FILTERS.END_DATE' | translate}}</label>
              <input formControlName="to" type="date" class="mal-input">
            </div>

            <div class="mal-filter-item" *ngIf="filterConfig.showUserFilter">
              <label class="mal-label">{{'AUDIT.FILTERS.USER' | translate}}</label>
              <input formControlName="user" type="text" class="mal-input" placeholder="{{'AUDIT.FILTERS.USER' | translate}}">
            </div>

            <div class="mal-filter-item" *ngIf="filterConfig.showApiFilter">
              <label class="mal-label">{{'AUDIT.FILTERS.API' | translate}}</label>
              <input formControlName="apiPath" type="text" class="mal-input" placeholder="{{'AUDIT.FILTERS.API' | translate}}">
            </div>

            <div class="mal-filter-item" *ngIf="filterConfig.showOperationFilter">
              <label class="mal-label">{{'AUDIT.FILTERS.OPERATION' | translate}}</label>
              <input formControlName="operation" type="text" class="mal-input" placeholder="{{'AUDIT.FILTERS.OPERATION' | translate}}">
            </div>

            <div class="mal-filter-item" *ngIf="filterConfig.showEntityFilter">
              <label class="mal-label">{{'AUDIT.FILTERS.ENTITY' | translate}}</label>
              <select formControlName="entity" class="mal-select">
                <option value="">{{'AUDIT.FILTERS.ALL' | translate}}</option>
                <option *ngFor="let entity of entityList" [value]="entity">{{entity}}</option>
              </select>
            </div>
          </div>
          
          <div class="mal-filter-actions">
            <button type="button" (click)="onFilterSubmit()" class="mal-button mal-button-primary">
              🔍 {{'LIST-FILTERS.SEARCH-BUTTON-TEXT' | translate}}
            </button>
            <button type="button" (click)="onClearFilters()" class="mal-button mal-button-secondary">
              🗑️ {{'LIST-FILTERS.CLEAR-BUTTON-TEXT' | translate}}
            </button>
          </div>
        </form>
      </div>

      <div class="mal-table-card" *ngIf="!isLoading">
        <div class="mal-table-container">
          <table class="mal-table">
            <thead>
              <tr>
                <th>{{'AUDIT.TABLE.ACTION' | translate}}</th>
                <th>{{'AUDIT.TABLE.USER' | translate}}</th>
                <th>{{'AUDIT.TABLE.ORIGIN' | translate}}</th>
                <th>{{'AUDIT.TABLE.OPERATION' | translate}}</th>
                <th>{{'AUDIT.TABLE.ENTITY_NAME' | translate}}</th>
                <th>{{'AUDIT.TABLE.NAVIGATED_TO' | translate}}</th>
                <th>{{'AUDIT.TABLE.API_PATH' | translate}}</th>
                <th>{{'AUDIT.TABLE.METHOD' | translate}}</th>
                <th>{{'AUDIT.TABLE.TIME' | translate}}</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let auditEntry of tableDataSource.data" (click)="onRowClick(auditEntry)" class="mal-table-row">
                <td>{{auditEntry.action}}</td>
                <td>{{auditEntry.actor}}</td>
                <td>{{auditEntry.origin}}</td>
                <td>{{auditEntry['operation'] || getElementValue(auditEntry.elements, 'operation')}}</td>
                <td>{{auditEntry['entityName'] || getElementValue(auditEntry.elements, 'entityName')}}</td>
                <td>{{auditEntry['navigatedTo'] || getElementValue(auditEntry.elements, 'page') || getElementValue(auditEntry.elements, 'urlAfterRedirects') || getElementValue(auditEntry.elements, 'entityName')}}</td>
                <td>{{auditEntry['APIPath']}}</td>
                <td>{{auditEntry['httpMethod'] || getElementValue(auditEntry.elements, 'httpMethod')}}</td>
                <td>{{auditEntry.timestamp | date:'medium'}}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="mal-loading" *ngIf="isLoading">
        <div class="mal-spinner"></div>
        <p>{{'AUDIT.LOADING' | translate}}</p>
      </div>
      
      <!-- Load More Button -->
      <div class="mal-load-more" *ngIf="!isLoading && hasMoreData && allData.length > 0">
        <button type="button" (click)="loadMoreData()" class="mal-button mal-button-secondary">
          📄 Load More ({{ allData.length }} loaded)
        </button>
      </div>
      
      <!-- No More Data Message -->
      <div class="mal-no-more-data" *ngIf="!hasMoreData && allData.length > 0">
        <p>✅ All data loaded ({{ allData.length }} records)</p>
      </div>
    </div>
  `,
  styles: [`
    .mal-audit-container {
      padding: 20px;
      font-family: 'Roboto', sans-serif;
    }
    
    .mal-header {
      margin-bottom: 20px;
    }
    
    .mal-title {
      color: var(--mal-primary-color, var(--mat-sys-primary));
      margin: 0;
      font-size: 24px;
      font-weight: 500;
    }
    
    .mal-card {
      background: var(--mal-card-bg, var(--mat-sys-surface));
      box-shadow: var(--mal-card-shadow, 0 4px 12px rgba(76, 175, 80, 0.1));
      margin-bottom: 20px;
      border-radius: 12px;
      border: 1px solid rgba(76, 175, 80, 0.1);
      overflow: hidden;
    }
    
    .mal-filter-form {
      display: flex;
      flex-direction: column;
      gap: 15px;
    }
    
    .mal-filter-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 20px;
      padding: 20px;
    }
    
    .mal-filter-item {
      display: flex;
      flex-direction: column;
    }
    
    .mal-label {
      font-weight: 600;
      color: var(--mal-text-primary, var(--mat-sys-on-surface));
      margin-bottom: 8px;
      display: block;
      font-size: 14px;
    }
    
    .mal-input,
    .mal-select {
      width: 100%;
      padding: 14px 16px;
      border: 2px solid var(--mal-filter-border, #e0e0e0);
      border-radius: 8px;
      font-size: 14px;
      background-color: var(--mal-filter-bg, var(--mat-sys-surface-container));
      transition: all 0.3s ease;
      box-sizing: border-box;
    }
    
    .mal-input:focus,
    .mal-select:focus {
      outline: none;
      border-color: var(--mal-primary-color, var(--mat-sys-primary));
      box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.1);
      background-color: var(--mal-input-bg, var(--mat-sys-surface));
      transform: translateY(-1px);
    }
    
    .mal-input:hover,
    .mal-select:hover {
      border-color: var(--mal-primary-color, var(--mat-sys-primary));
      background-color: var(--mal-input-bg, var(--mat-sys-surface));
    }
    
    .mal-input::placeholder {
      color: var(--mal-text-secondary, var(--mat-sys-on-surface-variant));
      font-style: italic;
    }
    
    .mal-filter-actions {
      display: flex;
      gap: 12px;
      margin-top: 20px;
      padding: 0 20px 20px 20px;
      justify-content: flex-end;
    }
    
    .mal-button {
      padding: 14px 28px;
      border: none;
      border-radius: 8px;
      font-size: 14px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s ease;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      min-width: 120px;
      justify-content: center;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
    
    .mal-button-primary {
      background: var(--mal-button-bg, var(--mat-sys-primary));
      color: var(--mal-button-text, var(--mat-sys-on-primary));
      box-shadow: 0 2px 4px rgba(76, 175, 80, 0.2);
    }
    
    .mal-button-primary:hover {
      background: var(--mal-button-hover, var(--mat-sys-primary-container));
      transform: translateY(-2px);
      box-shadow: 0 4px 8px rgba(76, 175, 80, 0.3);
    }
    
    .mal-button-primary:active {
      transform: translateY(0);
      box-shadow: 0 2px 4px rgba(76, 175, 80, 0.2);
    }
    
    .mal-button-secondary {
      background: transparent;
      color: var(--mal-primary-color, var(--mat-sys-primary));
      border: 2px solid var(--mal-primary-color, #4caf50);
    }
    
    .mal-button-secondary:hover {
      background: var(--mal-primary-color, #4caf50);
      color: var(--mal-button-text, var(--mat-sys-on-primary));
      transform: translateY(-2px);
      box-shadow: 0 4px 8px rgba(76, 175, 80, 0.2);
    }
    
    .mal-button-secondary:active {
      transform: translateY(0);
    }
    
    .mal-table-card {
      overflow: hidden;
    }
    
    .mal-table-container {
      overflow-x: auto;
    }
    
    .mal-table {
      width: 100%;
    }
    
    .mal-table th {
      background: var(--mal-table-header-bg, var(--mat-sys-surface-variant));
      font-weight: 600;
    }
    
    .mal-table-row {
      cursor: pointer;
    }
    
    .mal-table-row:hover {
      background: var(--mal-table-hover, var(--mat-sys-primary-container));
    }
    
    .mal-table-row:nth-child(even) {
      background: var(--mal-table-row-even, var(--mat-sys-surface));
    }
    
    .mal-table-row:nth-child(odd) {
      background: var(--mal-table-row-odd, var(--mat-sys-surface-container-low));
    }
    
    .mal-loading {
      text-align: center;
      padding: 40px;
    }
    
    .mal-loading p {
      margin-top: 16px;
      color: var(--mal-text-secondary, var(--mat-sys-on-surface-variant));
    }
    
    .mal-spinner {
      width: 40px;
      height: 40px;
      border: 4px solid #f3f3f3;
      border-top: 4px solid var(--mal-primary-color, #1976d2);
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin: 0 auto;
    }
    
    .mal-load-more {
      text-align: center;
      margin: 20px 0;
    }
    
    .mal-no-more-data {
      text-align: center;
      margin: 20px 0;
      color: var(--mal-text-secondary, #666);
      font-style: italic;
    }
    
    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
  `]
})
export class AuditComponent implements OnInit, OnChanges, OnDestroy {
  @Input() dataSource: IAudit[] = [];
  @Input() filterConfig: AuditFilterConfig = {
    showUserFilter: true,
    showDateFilter: true,
    showApiFilter: true,
    showOperationFilter: true,
    showEntityFilter: true  // Enabled - backend supports entityName filtering
  };
  @Input() entityList: string[] = [];
  @Input() isLoading: boolean = false;
  @Input() title: string = 'Audit';
  
  // Pagination properties
  currentOffset: number = 0;
  pageSize: number = 1000; // Load 1000 records at once
  hasMoreData: boolean = true;
  allData: IAudit[] = [];
  
  @Output() filterApplied = new EventEmitter<any>();
  @Output() clearFilters = new EventEmitter<void>();
  @Output() rowClicked = new EventEmitter<IAudit>();

  filterForm: FormGroup;
  tableDataSource: { data: IAudit[] } = { data: [] };
  displayedColumns: string[] = ['action', 'actor', 'origin', 'operation', 'entityName', 'navigatedTo', 'APIPath', 'timestamp'];

  constructor(
    private fb: FormBuilder,
    private translate: TranslateService,
    private http: HttpClient
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
    
    // If no data provided, load real data from API
    if (!this.dataSource || this.dataSource.length === 0) {
      this.loadRealData();
    }
    
    // Load entities for filter dropdown
    this.loadEntities();
    
    // Setup scroll listener for infinite pagination
    this.setupScrollListener();
    
    // Also setup table container scroll listener
    setTimeout(() => this.setupTableScrollListener(), 1000);
  }

  ngOnChanges() {
    if (this.dataSource) {
      this.tableDataSource.data = this.dataSource;
    }
  }

  onFilterSubmit() {
    const filterValue = this.filterForm.value;
    console.log('🔍 Filter applied:', filterValue);
    
    // Reset pagination when filtering
    this.currentOffset = 0;
    this.hasMoreData = true;
    this.allData = [];
    
    // Load filtered data from API
    this.loadFilteredData(filterValue);
    
    // Emit event for parent component
    this.filterApplied.emit(filterValue);
  }

  onClearFilters() {
    this.filterForm.reset();
    console.log('🗑️ Filters cleared');
    
    // Reset pagination when clearing filters
    this.currentOffset = 0;
    this.hasMoreData = true;
    this.allData = [];
    
    // Reload original data from API
    this.loadRealData();
    
    // Emit event for parent component
    this.clearFilters.emit();
  }

  private loadFilteredData(filterValue: any) {
    console.log('🔍 Loading filtered data from API...');
    this.isLoading = true;
    
    // Build search query from filter values - using audit dependency format
    // Format: fieldName[operator]=value;fieldName2[operator]=value2
    let searchQuery = '';
    
    // User filter (working)
    if (filterValue.user) {
      searchQuery += `actor[like]=${filterValue.user};`;
    }
    
    // API Path filter - using 'path' field which is more specific for API paths
    if (filterValue.apiPath) {
      searchQuery += `path[like]=${filterValue.apiPath};`;
    }
    
    // Operation filter - using 'operation' field which is more specific for CRUD operations
    if (filterValue.operation) {
      searchQuery += `operation[like]=${filterValue.operation};`;
    }
    
    // Entity filter
    if (filterValue.entity) {
      searchQuery += `entityName[like]=${filterValue.entity};`;
    }
    
    // Date range filter - using eventTime field and proper format
    if (filterValue.from || filterValue.to) {
      let dateRange = '';
      if (filterValue.from) {
        // Convert date to yyyy-MM-dd format
        const fromDate = new Date(filterValue.from).toISOString().split('T')[0];
        dateRange += fromDate;
      }
      dateRange += ',';
      if (filterValue.to) {
        // Convert date to yyyy-MM-dd format
        const toDate = new Date(filterValue.to).toISOString().split('T')[0];
        dateRange += toDate;
      }
      searchQuery += `eventTime[range]=${dateRange};`;
    }

    console.log('🔍 Search query built:', searchQuery);

    const primaryUrl = this.getApiUrl();
    const fallbackUrl = this.getFallbackApiUrl();
    
    console.log('🌐 Trying primary URL for filtered data:', primaryUrl);
    
    this.tryLoadFilteredData(primaryUrl, fallbackUrl, searchQuery);
  }

  private tryLoadFilteredData(primaryUrl: string, fallbackUrl: string, searchQuery: string) {
    const params: any = {
      offset: this.currentOffset.toString(),
      limit: this.pageSize.toString()
    };
    
    if (searchQuery) {
      params.search = searchQuery;
    }
    
    this.http.get(`${primaryUrl}/audit`, { params }).subscribe({
      next: (data: any) => {
        console.log('✅ Filtered data loaded successfully from primary URL:', primaryUrl);
        this.handleDataSuccess(data);
      },
      error: (error) => {
        console.warn('⚠️ Primary URL failed for filtered data:', primaryUrl, error);
        console.log('🔄 Trying fallback URL for filtered data:', fallbackUrl);
        
        // Try fallback URL
        this.http.get(`${fallbackUrl}/audit`, { params }).subscribe({
          next: (data: any) => {
            console.log('✅ Filtered data loaded successfully from fallback URL:', fallbackUrl);
            this.handleDataSuccess(data);
          },
          error: (fallbackError) => {
            console.error('❌ Both URLs failed for filtered data:', fallbackError);
            this.isLoading = false;
            this.tableDataSource.data = [];
          }
        });
      }
    });
  }


  onRowClick(row: IAudit) {
    this.rowClicked.emit(row);
  }

  getElementValue(elements: Array<{name: string, value: string}>, name: string): string {
    const element = elements.find(el => el.name === name);
    return element ? element.value : '';
  }

  private loadRealData() {
    console.log('📊 Loading real data from API...');
    this.isLoading = true;
    
    const primaryUrl = this.getApiUrl();
    const fallbackUrl = this.getFallbackApiUrl();
    
    console.log('🌐 Trying primary URL:', primaryUrl);
    
    this.tryLoadData(primaryUrl, fallbackUrl);
  }

  private tryLoadData(primaryUrl: string, fallbackUrl: string) {
    this.http.get(`${primaryUrl}/audit`, {
      params: {
        offset: this.currentOffset.toString(),
        limit: this.pageSize.toString()
      }
    }).subscribe({
      next: (data: any) => {
        console.log('✅ Data loaded successfully from primary URL:', primaryUrl);
        this.handleDataSuccess(data);
      },
      error: (error) => {
        console.warn('⚠️ Primary URL failed:', primaryUrl, error);
        console.log('🔄 Trying fallback URL:', fallbackUrl);
        
        // Try fallback URL
        this.http.get(`${fallbackUrl}/audit`, {
          params: {
            offset: this.currentOffset.toString(),
            limit: this.pageSize.toString()
          }
        }).subscribe({
          next: (data: any) => {
            console.log('✅ Data loaded successfully from fallback URL:', fallbackUrl);
            this.handleDataSuccess(data);
          },
          error: (fallbackError) => {
            console.error('❌ Both URLs failed:', fallbackError);
            this.isLoading = false;
            this.tableDataSource.data = [];
          }
        });
      }
    });
  }

  private handleDataSuccess(data: any) {
    console.log('📊 API data received:', data);
    
    // Transform API data to library format
    const transformedData = this.transformApiDataToLibraryFormat(data);
    
    // If this is the first page, replace data; otherwise append
    if (this.currentOffset === 0) {
      this.allData = transformedData;
    } else {
      this.allData = [...this.allData, ...transformedData];
    }
    
    this.tableDataSource.data = this.allData;
    this.isLoading = false;
    
    // Check if we have more data
    this.hasMoreData = transformedData.length === this.pageSize;
    
    console.log(`✅ Loaded ${transformedData.length} audit records from API (Total: ${this.allData.length})`);
  }

  private getApiUrl(): string {
    // Try to get API URL from environment or use default
    if (typeof window !== 'undefined' && (window as any).environment?.apiUrl) {
      return (window as any).environment.apiUrl;
    }
    
    // Default API URL - using same URL as existing services (localhost instead of 127.0.0.1)
    return 'https://localhost:5555';
  }

  private getFallbackApiUrl(): string {
    // Fallback URL in case primary URL fails
    return 'https://127.0.0.1:5555';
  }

  private transformApiDataToLibraryFormat(apiData: any[]): IAudit[] {
    if (!Array.isArray(apiData)) {
      return [];
    }

    return apiData.map(item => {
      console.log('🔍 Raw API item:', item); // Debug log
      
      return {
        action: item.action || 'UNKNOWN',
        actor: item.actor || 'Unknown User',
        origin: item.origin || 'Unknown',
        timestamp: item.timestamp || new Date().toISOString(),
        elements: item.elements || [],
        APIPath: this.getElementValue(item.elements, 'path') || '',
        navigatedTo: this.getElementValue(item.elements, 'page') || this.getElementValue(item.elements, 'urlAfterRedirects') || this.getElementValue(item.elements, 'entityName') || '',
        // Add operation, entityName, and httpMethod as direct fields for table display
        operation: this.getElementValue(item.elements, 'operation') || '',
        entityName: this.getElementValue(item.elements, 'entityName') || '',
        httpMethod: this.getElementValue(item.elements, 'httpMethod') || ''
      };
    });
  }

  private extractEntityName(path: string): string {
    if (!path) return 'Unknown';
    
    // Extract entity name from API path
    const pathParts = path.split('/').filter(part => part && part !== 'api');
    return pathParts[0] || 'Unknown';
  }

  private loadEntities() {
    console.log('🏢 Loading entities from API...');
    
    const primaryUrl = this.getApiUrl();
    const fallbackUrl = this.getFallbackApiUrl();
    
    console.log('🌐 Trying primary URL for entities:', primaryUrl);
    
    this.tryLoadEntities(primaryUrl, fallbackUrl);
  }

  private tryLoadEntities(primaryUrl: string, fallbackUrl: string) {
    this.http.get(`${primaryUrl}/audit/entities`).subscribe({
      next: (entities: any) => {
        console.log('✅ Entities loaded successfully from primary URL:', primaryUrl);
        this.entityList = Array.isArray(entities) ? entities : [];
      },
      error: (error) => {
        console.warn('⚠️ Primary URL failed for entities:', primaryUrl, error);
        console.log('🔄 Trying fallback URL for entities:', fallbackUrl);
        
        // Try fallback URL
        this.http.get(`${fallbackUrl}/audit/entities`).subscribe({
          next: (entities: any) => {
            console.log('✅ Entities loaded successfully from fallback URL:', fallbackUrl);
            this.entityList = Array.isArray(entities) ? entities : [];
          },
          error: (fallbackError) => {
            console.error('❌ Both URLs failed for entities:', fallbackError);
            // Set default entities if both APIs fail
            this.entityList = ['User', 'Project', 'Customer', 'Timesheet', 'Audit'];
          }
        });
      }
    });
  }

  private setupScrollListener() {
    console.log('🔧 Setting up scroll listener...');
    
    // Listen for scroll events on the window
    const scrollHandler = () => {
      const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
      const windowHeight = window.innerHeight;
      const documentHeight = document.documentElement.scrollHeight;
      
      // Calculate how far from bottom
      const distanceFromBottom = documentHeight - (scrollTop + windowHeight);
      
      console.log('📜 Scroll event:', {
        scrollTop,
        windowHeight,
        documentHeight,
        distanceFromBottom,
        isLoading: this.isLoading,
        hasMoreData: this.hasMoreData,
        currentOffset: this.currentOffset
      });
      
      if (this.isLoading || !this.hasMoreData) {
        console.log('📜 Skipping load - isLoading:', this.isLoading, 'hasMoreData:', this.hasMoreData);
        return;
      }
      
      // Load more data when user is 1000px from bottom (easy trigger)
      if (distanceFromBottom <= 1000) {
        console.log('📄 Scroll detected - loading more data... Distance from bottom:', distanceFromBottom);
        this.loadMoreData();
      }
    };
    
    // Add scroll listener with throttling
    let scrollTimeout: any;
    const throttledScrollHandler = () => {
      if (scrollTimeout) return;
      scrollTimeout = setTimeout(() => {
        scrollHandler();
        scrollTimeout = null;
      }, 100); // Throttle to 100ms
    };
    
    window.addEventListener('scroll', throttledScrollHandler, { passive: true });
    
    // Store reference for cleanup
    (this as any).scrollHandler = throttledScrollHandler;
    
    console.log('✅ Scroll listener setup complete');
  }

  loadMoreData() {
    if (this.isLoading || !this.hasMoreData) {
      console.log('📄 Cannot load more data - isLoading:', this.isLoading, 'hasMoreData:', this.hasMoreData);
      return;
    }
    
    console.log('📄 Loading more data... Current offset:', this.currentOffset);
    this.currentOffset += this.pageSize;
    
    // Check if we have active filters
    const filterValue = this.filterForm.value;
    const hasActiveFilters = filterValue.user || filterValue.apiPath || 
                           filterValue.operation || filterValue.entity || 
                           filterValue.from || filterValue.to;
    
    if (hasActiveFilters) {
      this.loadFilteredData(filterValue);
    } else {
      this.loadRealData();
    }
  }

  private setupTableScrollListener() {
    console.log('🔧 Setting up table scroll listener...');
    
    // Try to find the table container with multiple selectors
    const selectors = ['.mal-table-container', '.mal-table', 'table', '.mat-table'];
    let tableContainer: Element | null = null;
    
    for (const selector of selectors) {
      tableContainer = document.querySelector(selector);
      if (tableContainer) {
        console.log(`✅ Found table container with selector: ${selector}`);
        break;
      }
    }
    
    if (tableContainer) {
      const tableScrollHandler = () => {
        const scrollTop = tableContainer.scrollTop;
        const scrollHeight = tableContainer.scrollHeight;
        const clientHeight = tableContainer.clientHeight;
        const distanceFromBottom = scrollHeight - (scrollTop + clientHeight);
        
        console.log('📊 Table scroll event:', {
          scrollTop,
          scrollHeight,
          clientHeight,
          distanceFromBottom,
          isLoading: this.isLoading,
          hasMoreData: this.hasMoreData
        });
        
        if (this.isLoading || !this.hasMoreData) return;
        
        // Load more data when user is near bottom
        if (distanceFromBottom <= 50) {
          console.log('📄 Table scroll detected - loading more data... Distance from bottom:', distanceFromBottom);
          this.loadMoreData();
        }
      };
      
      // Add throttling to table scroll as well
      let tableScrollTimeout: any;
      const throttledTableScrollHandler = () => {
        if (tableScrollTimeout) return;
        tableScrollTimeout = setTimeout(() => {
          tableScrollHandler();
          tableScrollTimeout = null;
        }, 100);
      };
      
      tableContainer.addEventListener('scroll', throttledTableScrollHandler, { passive: true });
      (this as any).tableScrollHandler = throttledTableScrollHandler;
    } else {
      console.log('❌ No table container found, using window scroll only');
    }
  }

  ngOnDestroy() {
    // Clean up scroll listeners
    if ((this as any).scrollHandler) {
      window.removeEventListener('scroll', (this as any).scrollHandler);
    }
    if ((this as any).tableScrollHandler) {
      const tableContainer = document.querySelector('.mal-table-container');
      if (tableContainer) {
        tableContainer.removeEventListener('scroll', (this as any).tableScrollHandler);
      }
    }
  }
}