# Audit Library

A comprehensive Angular 20 library for audit trail management with Angular Material 20, real API integration, dynamic theming, and translation support.

## Features

- ✅ **Angular 20 Compatible** - Built with latest Angular 20 features
- ✅ **Angular Material 20** - Modern Material Design components
- ✅ **Standalone Components** - Modern Angular architecture
- ✅ **Real API Integration** - Direct integration with backend audit APIs
- ✅ **Dynamic Theming** - CSS custom properties that inherit from host application
- ✅ **Translation Support** - Built-in i18n with @ngx-translate
- ✅ **Advanced Filtering** - Real-time API-based filtering with multiple criteria
- ✅ **Type Safe** - Full TypeScript support
- ✅ **Responsive Design** - Mobile-friendly interface
- ✅ **Authentication Ready** - Works with Angular HTTP interceptors

## Installation

```bash
npm install audit-library
```

## Dependencies

Make sure you have the following peer dependencies installed:

```bash
npm install @angular/common@^20.3.0 @angular/core@^20.3.0 @angular/forms@^20.3.0 @angular/material@^20.3.0 @angular/cdk@^20.3.0 @ngx-translate/core@^15.0.0 @angular/common@^20.3.0
```

**Note:** The library requires `HttpClient` for API integration. Make sure to import `provideHttpClient()` in your application configuration.

## Basic Usage

### 1. Import the Module

```typescript
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideHttpClient } from '@angular/common/http';
import { provideTranslateService } from '@ngx-translate/core';
import { AuditLibraryModule } from 'audit-library';

@NgModule({
  imports: [
    BrowserModule,
    AuditLibraryModule.forRoot()
  ],
  providers: [
    provideAnimations(), // Required for Angular Material
    provideHttpClient(), // Required for API integration
    provideTranslateService({
      defaultLanguage: 'en'
    })
  ]
})
export class AppModule { }
```

### 2. Use the Component

```typescript
import { Component } from '@angular/core';
import { AuditComponent, IAudit, AuditFilterConfig } from 'audit-library';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [AuditComponent],
  template: `
    <mal-audit
      [filterConfig]="filterConfig"
      [title]="'Audit Dashboard'"
      (filterApplied)="onFilterApplied($event)"
      (clearFilters)="onFilterCleared()"
      (rowClicked)="onRowClicked($event)">
    </mal-audit>
  `
})
export class AppComponent {
  filterConfig: AuditFilterConfig = {
    showUserFilter: true,
    showDateFilter: true,
    showApiFilter: true,
    showOperationFilter: true,
    showEntityFilter: true // Enabled - backend supports entityName filtering
  };

  onFilterApplied(filter: any) {
    console.log('Filter applied:', filter);
    // The component automatically loads filtered data from API
  }

  onFilterCleared() {
    console.log('Filters cleared');
    // The component automatically reloads all data from API
  }

  onRowClicked(row: IAudit) {
    console.log('Row clicked:', row);
  }
}
```

**Note:** The component now automatically loads data from the API. You don't need to provide `dataSource` unless you want to override the API data.

## API Integration

The library automatically integrates with your backend audit APIs. It supports the following endpoints:

### Supported Endpoints

- **GET /audit** - Retrieves audit data with pagination and filtering
- **GET /audit/entities** - Retrieves available entity names for filtering

### API Configuration

The library automatically detects your API URL from the environment or uses a default. You can configure it by:

1. **Environment Configuration:**
```typescript
// In your environment files
export const environment = {
  apiUrl: 'https://your-api-domain.com'
};
```

2. **Default Configuration:**
The library defaults to `https://127.0.0.1:5555` if no environment configuration is found.

### Authentication

The library uses Angular's `HttpClient` and automatically includes authentication headers from your HTTP interceptors. Make sure you have configured your authentication interceptors in your application.

### Filtering

The library supports advanced filtering with the following search format:
- **User Filter:** `actor[like]=username`
- **API Path Filter:** `origin[like]=/api/path`
- **Operation Filter:** `action[like]=CREATE`
- **Entity Filter:** `entityName[like]=User`
- **Date Range Filter:** `timestamp[range]=startDate,endDate`

## Custom Theming

### 1. Dynamic Theme Configuration (Recommended)

The library now supports dynamic theming that automatically inherits from your application's theme:

```typescript
import { AuditLibraryModule } from 'audit-library';

// Factory function to get theme from CSS variables
export function getThemeFromCSS(): any {
  if (typeof document !== 'undefined') {
    const root = document.documentElement;
    const computedStyle = getComputedStyle(root);

    return {
      primaryColor: computedStyle.getPropertyValue('--mal-primary-color').trim() ||
                    computedStyle.getPropertyValue('--mat-sys-primary').trim() || '#1976d2',
      accentColor: computedStyle.getPropertyValue('--mal-accent-color').trim() ||
                   computedStyle.getPropertyValue('--mat-sys-tertiary').trim() || '#ff4081',
      // ... other theme properties
    };
  }
  return { /* fallback values */ };
}

@NgModule({
  imports: [
    AuditLibraryModule.forRoot(getThemeFromCSS())
  ]
})
export class AppModule { }
```

### 2. Static Theme Configuration

```typescript
import { AuditLibraryModule, AuditThemeConfig } from 'my-audit-library';

const customTheme: AuditThemeConfig = {
  primaryColor: '#1976d2',
  accentColor: '#ff4081',
  warnColor: '#f44336',
  filterBackground: '#f0f8ff',
  filterBorder: '#b3d9ff',
  tableHeaderBackground: '#e3f2fd',
  tableRowEven: '#fafafa',
  tableRowOdd: '#ffffff',
  tableHover: '#f5f5f5',
  buttonBackground: '#1976d2',
  buttonTextColor: '#ffffff',
  cardBackground: '#ffffff',
  cardShadow: '0 2px 4px rgba(0,0,0,0.1)'
};

@NgModule({
  imports: [
    AuditLibraryModule.forRoot(customTheme)
  ]
})
export class AppModule { }
```

### 3. Dynamic Theme Updates

```typescript
import { AuditThemeService } from 'audit-library';

constructor(private themeService: AuditThemeService) {}

updateTheme() {
  this.themeService.updateTheme({
    primaryColor: '#ff5722',
    buttonBackground: '#ff5722'
  });
}
```

### 4. CSS Variables Integration

Add these CSS variables to your global styles to enable dynamic theming:

```css
:root {
  /* Audit Library Theme Variables - inherits from Material Design */
  --mal-primary-color: var(--mat-sys-primary);
  --mal-accent-color: var(--mat-sys-tertiary);
  --mal-warn-color: var(--mat-sys-error);
  --mal-filter-bg: var(--mat-sys-surface-container);
  --mal-filter-border: var(--mat-sys-outline);
  --mal-table-header-bg: var(--mat-sys-surface-variant);
  --mal-table-row-even: var(--mat-sys-surface);
  --mal-table-row-odd: var(--mat-sys-surface-container-low);
  --mal-table-hover: var(--mat-sys-primary-container);
  --mal-button-bg: var(--mat-sys-primary);
  --mal-button-text: var(--mat-sys-on-primary);
  --mal-card-bg: var(--mat-sys-surface);
  --mal-card-shadow: var(--mat-sys-elevation-level1);
  --mal-text-primary: var(--mat-sys-on-surface);
  --mal-text-secondary: var(--mat-sys-on-surface-variant);
  --mal-input-bg: var(--mat-sys-surface);
  --mal-spinner-bg: var(--mat-sys-surface-variant);
}
```

## Translation Support

### 1. Configure Translations

```typescript
import { AuditLibraryModule, AuditTranslationConfig } from 'my-audit-library';

const customTranslations: AuditTranslationConfig = {
  defaultLanguage: 'en',
  translations: {
    en: {
      'AUDIT.TITLE': 'Audit Trail Dashboard',
      'AUDIT.FILTERS.START_DATE': 'From Date',
      'AUDIT.FILTERS.END_DATE': 'To Date',
      'AUDIT.FILTERS.USER': 'User',
      'AUDIT.FILTERS.API': 'API Path',
      'AUDIT.FILTERS.OPERATION': 'Operation',
      'AUDIT.FILTERS.ENTITY': 'Entity',
      'AUDIT.FILTERS.ALL': 'All',
      'AUDIT.TABLE.ACTION': 'Action',
      'AUDIT.TABLE.USER': 'User',
      'AUDIT.TABLE.ORIGIN': 'Origin',
      'AUDIT.TABLE.OPERATION': 'Operation',
      'AUDIT.TABLE.ENTITY_NAME': 'Entity Name',
      'AUDIT.TABLE.NAVIGATED_TO': 'Navigated To',
      'AUDIT.TABLE.API_PATH': 'API Path',
      'AUDIT.TABLE.TIME': 'Timestamp',
      'LIST-FILTERS.SEARCH-BUTTON-TEXT': 'Search',
      'LIST-FILTERS.CLEAR-BUTTON-TEXT': 'Clear',
      'AUDIT.LOADING': 'Loading...'
    },
    fr: {
      'AUDIT.TITLE': 'Tableau de Bord des Audits',
      'AUDIT.FILTERS.START_DATE': 'Date de Début',
      'AUDIT.FILTERS.END_DATE': 'Date de Fin',
      // ... more translations
    }
  }
};

@NgModule({
  imports: [
    AuditLibraryModule.forRoot(undefined, customTranslations)
  ]
})
export class AppModule { }
```

### 2. Dynamic Language Changes

```typescript
import { AuditTranslationService } from 'audit-library';

constructor(private translationService: AuditTranslationService) {}

changeLanguage(lang: string) {
  this.translationService.setLanguage(lang);
}
```

## API Reference

### AuditComponent

#### Inputs

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `dataSource` | `IAudit[]` | `[]` | Array of audit data (optional - component loads from API if not provided) |
| `filterConfig` | `AuditFilterConfig` | `{...}` | Configuration for filter visibility |
| `entityList` | `string[]` | `[]` | List of entities for dropdown (optional - component loads from API) |
| `isLoading` | `boolean` | `false` | Loading state (managed automatically) |
| `title` | `string` | `'Audit Trail'` | Component title |

#### Outputs

| Event | Type | Description |
|-------|------|-------------|
| `filterApplied` | `EventEmitter<any>` | Emitted when filters are applied |
| `clearFilters` | `EventEmitter<void>` | Emitted when filters are cleared |
| `rowClicked` | `EventEmitter<IAudit>` | Emitted when a table row is clicked |

### Interfaces

#### IAudit

```typescript
interface IAudit {
  action: string;           // The action performed (CREATE, UPDATE, DELETE, etc.)
  actor: string;            // The user who performed the action
  origin: string;           // The origin/source of the action
  timestamp: string;        // ISO timestamp of when the action occurred
  elements: Array<{name: string, value: string}>; // Additional audit details
  APIPath?: string;         // API path if applicable
  navigatedTo?: string;     // Navigation target if applicable
  [key: string]: any;       // Additional properties
}
```

#### AuditFilterConfig

```typescript
interface AuditFilterConfig {
  showUserFilter?: boolean;      // Show user filter (default: true)
  showDateFilter?: boolean;      // Show date range filter (default: true)
  showApiFilter?: boolean;       // Show API path filter (default: true)
  showOperationFilter?: boolean; // Show operation filter (default: true)
  showEntityFilter?: boolean;    // Show entity filter (default: true - backend supports entityName filtering)
}
```

#### AuditThemeConfig

```typescript
interface AuditThemeConfig {
  primaryColor?: string;
  accentColor?: string;
  warnColor?: string;
  filterBackground?: string;
  filterBorder?: string;
  tableHeaderBackground?: string;
  tableRowEven?: string;
  tableRowOdd?: string;
  tableHover?: string;
  buttonBackground?: string;
  buttonTextColor?: string;
  cardBackground?: string;
  cardShadow?: string;
}
```

## CSS Custom Properties

The library uses CSS custom properties for theming. You can override these in your global styles:

```css
:root {
  /* Audit Library Theme Variables - inherits from Material Design */
  --mal-primary-color: var(--mat-sys-primary);
  --mal-accent-color: var(--mat-sys-tertiary);
  --mal-warn-color: var(--mat-sys-error);
  --mal-filter-bg: var(--mat-sys-surface-container);
  --mal-filter-border: var(--mat-sys-outline);
  --mal-table-header-bg: var(--mat-sys-surface-variant);
  --mal-table-row-even: var(--mat-sys-surface);
  --mal-table-row-odd: var(--mat-sys-surface-container-low);
  --mal-table-hover: var(--mat-sys-primary-container);
  --mal-button-bg: var(--mat-sys-primary);
  --mal-button-text: var(--mat-sys-on-primary);
  --mal-card-bg: var(--mat-sys-surface);
  --mal-card-shadow: var(--mat-sys-elevation-level1);
  --mal-text-primary: var(--mat-sys-on-surface);
  --mal-text-secondary: var(--mat-sys-on-surface-variant);
  --mal-input-bg: var(--mat-sys-surface);
  --mal-spinner-bg: var(--mat-sys-surface-variant);
}
```

## Recent Updates

### Version 2.0.0 - Major Updates

- ✅ **Real API Integration** - Component now automatically loads data from backend APIs
- ✅ **Dynamic Theming** - Theme automatically inherits from host application's CSS variables
- ✅ **Improved Filtering** - Advanced API-based filtering with proper search query format
- ✅ **Authentication Support** - Works seamlessly with Angular HTTP interceptors
- ✅ **Enhanced Styling** - Improved filter fields and button styling
- ✅ **Better Error Handling** - Graceful fallbacks for API failures
- ✅ **Performance Optimized** - Efficient data loading and transformation

### Breaking Changes

- `dataSource` input is now optional - component loads data from API by default
- `entityList` input is now optional - component loads entities from API
- `showEntityFilter` defaults to `true` (backend supports entityName filtering)
- Theme configuration now supports dynamic CSS variable reading

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## License

MIT

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Support

For support, email support@example.com or create an issue in the repository.