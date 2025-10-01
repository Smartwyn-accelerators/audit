import { Injectable, Inject, Optional, InjectionToken } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { AuditTranslationConfig } from '../types/audit.types';

// Injection Token for Translation Configuration
export const AUDIT_TRANSLATION_CONFIG = new InjectionToken<AuditTranslationConfig>('AUDIT_TRANSLATION_CONFIG');

@Injectable({
  providedIn: 'root'
})
export class AuditTranslationService {
  constructor(
    private translate: TranslateService,
    @Optional() @Inject(AUDIT_TRANSLATION_CONFIG) private translationConfig: AuditTranslationConfig
  ) {
    this.setupTranslations();
  }

  private setupTranslations(): void {
    if (this.translationConfig && this.translationConfig.translations) {
      // Set default language
      if (this.translationConfig.defaultLanguage) {
        this.translate.setDefaultLang(this.translationConfig.defaultLanguage);
      }

      // Add translations
      Object.keys(this.translationConfig.translations).forEach(lang => {
        this.translate.setTranslation(lang, this.translationConfig.translations![lang]);
      });
    }
  }

  setTranslations(lang: string, translations: any): void {
    this.translate.setTranslation(lang, translations);
  }

  getCurrentLanguage(): string {
    return this.translate.currentLang || this.translate.defaultLang || 'en';
  }

  setLanguage(lang: string): void {
    this.translate.use(lang);
  }
}
