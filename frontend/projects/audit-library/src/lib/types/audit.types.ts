// Audit Data Interface
export interface IAudit {
  action: string;
  actor: string;
  origin: string;
  timestamp: string;
  elements: Array<{name: string, value: string}>;
  [key: string]: any;
}

// Filter Configuration Interface
export interface AuditFilterConfig {
  showUserFilter?: boolean;
  showDateFilter?: boolean;
  showApiFilter?: boolean;
  showOperationFilter?: boolean;
  showEntityFilter?: boolean;
}

// Theme Configuration Interface
export interface AuditThemeConfig {
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

// Translation Configuration Interface
export interface AuditTranslationConfig {
  defaultLanguage?: string;
  translations?: {
    [language: string]: {
      [key: string]: string;
    };
  };
}
