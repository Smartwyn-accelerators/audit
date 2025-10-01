import { NgModule, ModuleWithProviders, InjectionToken } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

// Services
import { AuditThemeService, AUDIT_THEME_CONFIG } from './services/audit-theme.service';
import { AuditTranslationService, AUDIT_TRANSLATION_CONFIG } from './services/audit-translation.service';

// Components
import { AuditComponent } from './components/audit/audit.component';

// Types
import { AuditThemeConfig, AuditTranslationConfig } from './types/audit.types';

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TranslateModule,
    // Import standalone component
    AuditComponent
  ],
  exports: [
    // Export modules for consuming applications
    CommonModule,
    ReactiveFormsModule,
    TranslateModule,
    // Export the component
    AuditComponent
  ]
})
export class AuditLibraryModule {
  /**
   * Configure the library with custom theme and translation settings
   * @param themeConfig - Custom theme configuration
   * @param translationConfig - Custom translation configuration
   */
  static forRoot(
    themeConfig?: AuditThemeConfig,
    translationConfig?: AuditTranslationConfig
  ): ModuleWithProviders<AuditLibraryModule> {
    return {
      ngModule: AuditLibraryModule,
      providers: [
        AuditThemeService,
        AuditTranslationService,
        {
          provide: AUDIT_THEME_CONFIG,
          useValue: themeConfig || {}
        },
        {
          provide: AUDIT_TRANSLATION_CONFIG,
          useValue: translationConfig || {}
        }
      ]
    };
  }

  /**
   * Use this method for child modules (no configuration)
   */
  static forChild(): ModuleWithProviders<AuditLibraryModule> {
    return {
      ngModule: AuditLibraryModule,
      providers: [
        AuditThemeService,
        AuditTranslationService
      ]
    };
  }
}