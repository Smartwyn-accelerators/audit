import { Injectable, Inject, Optional, DOCUMENT, InjectionToken } from '@angular/core';
import { AuditThemeConfig } from '../types/audit.types';

// Injection Token for Theme Configuration
export const AUDIT_THEME_CONFIG = new InjectionToken<AuditThemeConfig>('AUDIT_THEME_CONFIG');

@Injectable({
  providedIn: 'root'
})
export class AuditThemeService {
  constructor(
    @Inject(DOCUMENT) private document: Document,
    @Optional() @Inject(AUDIT_THEME_CONFIG) private themeConfig: AuditThemeConfig
  ) {
    this.applyTheme();
  }

  private applyTheme(): void {
    if (this.themeConfig) {
      const root = this.document.documentElement;
      
      // Apply CSS custom properties for theming
      if (this.themeConfig.primaryColor) {
        root.style.setProperty('--mal-primary-color', this.themeConfig.primaryColor);
      }
      if (this.themeConfig.accentColor) {
        root.style.setProperty('--mal-accent-color', this.themeConfig.accentColor);
      }
      if (this.themeConfig.warnColor) {
        root.style.setProperty('--mal-warn-color', this.themeConfig.warnColor);
      }
      if (this.themeConfig.filterBackground) {
        root.style.setProperty('--mal-filter-bg', this.themeConfig.filterBackground);
      }
      if (this.themeConfig.filterBorder) {
        root.style.setProperty('--mal-filter-border', this.themeConfig.filterBorder);
      }
      if (this.themeConfig.tableHeaderBackground) {
        root.style.setProperty('--mal-table-header-bg', this.themeConfig.tableHeaderBackground);
      }
      if (this.themeConfig.tableRowEven) {
        root.style.setProperty('--mal-table-row-even', this.themeConfig.tableRowEven);
      }
      if (this.themeConfig.tableRowOdd) {
        root.style.setProperty('--mal-table-row-odd', this.themeConfig.tableRowOdd);
      }
      if (this.themeConfig.tableHover) {
        root.style.setProperty('--mal-table-hover', this.themeConfig.tableHover);
      }
      if (this.themeConfig.buttonBackground) {
        root.style.setProperty('--mal-button-bg', this.themeConfig.buttonBackground);
      }
      if (this.themeConfig.buttonTextColor) {
        root.style.setProperty('--mal-button-text', this.themeConfig.buttonTextColor);
      }
      if (this.themeConfig.cardBackground) {
        root.style.setProperty('--mal-card-bg', this.themeConfig.cardBackground);
      }
      if (this.themeConfig.cardShadow) {
        root.style.setProperty('--mal-card-shadow', this.themeConfig.cardShadow);
      }
    }
  }

  updateTheme(newTheme: AuditThemeConfig): void {
    this.themeConfig = { ...this.themeConfig, ...newTheme };
    this.applyTheme();
  }
}
