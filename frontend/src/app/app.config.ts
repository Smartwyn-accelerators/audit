import { ApplicationConfig, provideZoneChangeDetection, importProvidersFrom, inject, DOCUMENT } from '@angular/core';
import { provideRouter, withEnabledBlockingInitialNavigation } from '@angular/router';
import { provideHttpClient, withInterceptors, HttpClient } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideAuth } from 'angular-auth-oidc-client';   // <-- correct import
import { provideTranslateService, TranslateLoader } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { authConfig } from '../environments/environment';
import { AuditLibraryModule, AUDIT_THEME_CONFIG, AUDIT_TRANSLATION_CONFIG, AuditThemeService } from 'audit-library';
// Factory function for TranslateHttpLoader
export function httpLoaderFactory(http: HttpClient): TranslateHttpLoader {
  return new TranslateHttpLoader(http, './assets/i18n/', '.json');
}

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
      warnColor: computedStyle.getPropertyValue('--mal-warn-color').trim() || 
                computedStyle.getPropertyValue('--mat-sys-error').trim() || '#f44336',
      filterBackground: computedStyle.getPropertyValue('--mal-filter-bg').trim() || 
                       computedStyle.getPropertyValue('--mat-sys-surface-container').trim() || '#f5f5f5',
      filterBorder: computedStyle.getPropertyValue('--mal-filter-border').trim() || 
                   computedStyle.getPropertyValue('--mat-sys-outline').trim() || '#e0e0e0',
      tableHeaderBackground: computedStyle.getPropertyValue('--mal-table-header-bg').trim() || 
                            computedStyle.getPropertyValue('--mat-sys-surface-variant').trim() || '#fafafa',
      tableRowEven: computedStyle.getPropertyValue('--mal-table-row-even').trim() || 
                   computedStyle.getPropertyValue('--mat-sys-surface').trim() || '#ffffff',
      tableRowOdd: computedStyle.getPropertyValue('--mal-table-row-odd').trim() || 
                  computedStyle.getPropertyValue('--mat-sys-surface-container-low').trim() || '#f9f9f9',
      tableHover: computedStyle.getPropertyValue('--mal-table-hover').trim() || 
                 computedStyle.getPropertyValue('--mat-sys-primary-container').trim() || '#e3f2fd',
      buttonBackground: computedStyle.getPropertyValue('--mal-button-bg').trim() || 
                       computedStyle.getPropertyValue('--mat-sys-primary').trim() || '#1976d2',
      buttonTextColor: computedStyle.getPropertyValue('--mal-button-text').trim() || 
                      computedStyle.getPropertyValue('--mat-sys-on-primary').trim() || '#ffffff',
      cardBackground: computedStyle.getPropertyValue('--mal-card-bg').trim() || 
                     computedStyle.getPropertyValue('--mat-sys-surface').trim() || '#ffffff',
      cardShadow: computedStyle.getPropertyValue('--mal-card-shadow').trim() || 
                 computedStyle.getPropertyValue('--mat-sys-elevation-level1').trim() || '0 2px 4px rgba(0,0,0,0.1)'
    };
  }
  
  // Fallback values if document is not available
  return {
    primaryColor: '#1976d2',
    accentColor: '#ff4081',
    warnColor: '#f44336',
    filterBackground: '#f5f5f5',
    filterBorder: '#e0e0e0',
    tableHeaderBackground: '#fafafa',
    tableRowEven: '#ffffff',
    tableRowOdd: '#f9f9f9',
    tableHover: '#e3f2fd',
    buttonBackground: '#1976d2',
    buttonTextColor: '#ffffff',
    cardBackground: '#ffffff',
    cardShadow: '0 2px 4px rgba(0,0,0,0.1)'
  };
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withEnabledBlockingInitialNavigation()),
    provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
    provideAnimations(),
    provideAuth({
      config: authConfig
    }),
    provideTranslateService({
      defaultLanguage: 'en',
      useDefaultLang: true,
      loader: {
        provide: TranslateLoader,
        useFactory: httpLoaderFactory,
        deps: [HttpClient]
      }
    }),
    // Import Audit Library Module with dynamic theme configuration
    importProvidersFrom(
      AuditLibraryModule.forRoot(
        // Dynamic Theme Configuration - reads from styles.scss CSS variables
        getThemeFromCSS(),
        // Translation Configuration
        {
          defaultLanguage: 'en',
          translations: {
            en: {
              'audit.title': 'Audit',
              'audit.filter.startDate': 'Start Date',
              'audit.filter.endDate': 'End Date',
              'audit.filter.user': 'User',
              'audit.filter.search': 'Search',
              'audit.table.action': 'Action',
              'audit.table.actor': 'Actor',
              'audit.table.origin': 'Origin',
              'audit.table.timestamp': 'Timestamp',
              'audit.table.apiPath': 'API Path',
              'audit.table.elements': 'Elements',
              'audit.table.operation': 'Operation',
              'audit.table.entityName': 'Entity Name'
            }
          }
        }
      )
    )
  ]
};