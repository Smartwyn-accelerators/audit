package com.fastcode.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuditPropertiesConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(AuditPropertiesConfiguration.class);

    @Autowired
    private Environment env;

//    @Autowired
//    private AuditLayoutProperties auditLayoutProperties;

    private static final String FASTCODE_OFFSET_DEFAULT_ENV = "FASTCODE_OFFSET_DEFAULT";
    private static final String FASTCODE_OFFSET_DEFAULT_SYSPROP = "fastCode.offset.default";

    private static final String FASTCODE_LIMIT_DEFAULT_ENV = "FASTCODE_LIMIT_DEFAULT";
    private static final String FASTCODE_LIMIT_DEFAULT_SYSPROP = "fastCode.limit.default";

    private static final String FASTCODE_SORT_DIRECTION_DEFAULT_ENV = "FASTCODE_SORT_DIRECTION_DEFAULT";
    private static final String FASTCODE_SORT_DIRECTION_DEFAULT_SYSPROP = "fastCode.sort.direction.default";

    private static final String AUDIT_CONSOLE_ENABLED_ENV = "AUDIT_CONSOLE_ENABLED";
    private static final String AUDIT_CONSOLE_ENABLED_SYSPROP = "audit.console.enabled";

    private static final String AUDIT_FILE_ENABLED_ENV = "AUDIT_FILE_ENABLED";
    private static final String AUDIT_FILE_ENABLED_SYSPROP = "audit.file.enabled";

    private static final String AUDIT_DATABASE_ENABLED_ENV = "AUDIT_DATABASE_ENABLED";
    private static final String AUDIT_DATABASE_ENABLED_SYSPROP = "audit.database.enabled";

    private static final String AUDIT_FILE_PATH_ENV = "AUDIT_FILE_PATH";
    private static final String AUDIT_FILE_PATH_SYSPROP = "audit.file.path";

    private static final String AUDIT_FILE_PREFIX_ENV = "AUDIT_FILE_PREFIX";
    private static final String AUDIT_FILE_PREFIX_SYSPROP = "audit.file.prefix";

    private static final String SECURE_LAYOUT_ENABLED_ENV = "SECURE_LAYOUT_ENABLED";
    private static final String SECURE_LAYOUT_ENABLED_SYSPROP = "secure.layout.enabled";

    private static final String SECURE_LAYOUT_KEY_ENV = "SECURE_LAYOUT_KEY";
    private static final String SECURE_LAYOUT_KEY_SYSPROP = "secure.layout.key";

    private static final String SECURE_LAYOUT_SALT_ENV = "SECURE_LAYOUT_SALT";
    private static final String SECURE_LAYOUT_SALT_SYSPROP = "secure.layout.salt";

    private static final String ENCRYPTION_SECRET_ENABLED_ENV = "ENCRYPTION_SECRET_ENABLED";
    private static final String ENCRYPTION_SECRET_ENABLED_SYSPROP = "encryption.secret.enabled";

    private static final String ENCRYPTION_SECRET_KEY_ENV = "ENCRYPTION_SECRET_KEY";
    private static final String ENCRYPTION_SECRET_KEY_SYSPROP = "encryption.secret.key";

    private static final String AUDIT_LAYOUT_TEMPLATE_ENV = "AUDIT_LAYOUT_TEMPLATE";
    private static final String AUDIT_LAYOUT_TEMPLATE_SYSPROP = "audit.layout.template";

    private static final String AUDIT_API_DISABLED_ENV = "AUDIT_API_DISABLED";
    private static final String AUDIT_API_DISABLED_SYSPROP = "audit.api.disabled";

    private static final String AUDIT_ENTITY_DISABLED_ENV = "AUDIT_ENTITY_DISABLED";
    private static final String AUDIT_ENTITY_DISABLED_SYSPROP = "audit.entity.disabled";

    private static final String AUDIT_SECURITY_DISABLED_ENV = "AUDIT_SECURITY_DISABLED";
    private static final String AUDIT_SECURITY_DISABLED_SYSPROP = "audit.security.disabled";

    private static final String EXCLUDE_API_PATH_ENV = "EXCLUDE_API_PATH";
    private static final String EXCLUDE_API_PATH_SYSPROP = "exclude.api.path";

    private static final String AUDIT_DATABASE_URL_ENV = "AUDIT_DATABASE_URL";
    private static final String AUDIT_DATABASE_URL_SYSPROP = "audit.database.url";

    private static final String AUDIT_DATABASE_USERNAME_ENV = "AUDIT_DATABASE_USERNAME";
    private static final String AUDIT_DATABASE_USERNAME_SYSPROP = "audit.database.username";

    private static final String AUDIT_DATABASE_PASSWORD_ENV = "AUDIT_DATABASE_PASSWORD";
    private static final String AUDIT_DATABASE_PASSWORD_SYSPROP = "audit.database.password";

    private static final String AUDIT_API_PACKAGE_ENV = "AUDIT_API_PACKAGE";
    private static final String AUDIT_API_PACKAGE_SYSPROP = "audit.api.package";

    private static final String AUDIT_AUTH_HEADER_NAME_ENV = "AUDIT_AUTH_HEADER_NAME";
    private static final String AUDIT_AUTH_HEADER_NAME_SYSPROP = "audit.auth.header.name";

    private static final String AUDIT_JWT_SECRET_KEY_ENV = "AUDIT_JWT_SECRET_KEY";
    private static final String AUDIT_JWT_SECRET_KEY_SYSPROP = "audit.jwt.secret.key";

    private static final String OIDC_ISSUER_URI_ENV = "OIDC_ISSUER_URI";
    private static final String OIDC_ISSUER_URI_SYSPROP = "spring.security.oauth2.client.provider.oidc.issuer-uri";

    private static final String SENSITIVE_DATA_MASKING_ENABLED_ENV = "SENSITIVE_DATA_MASKING_ENABLED";
    private static final String SENSITIVE_DATA_MASKING_ENABLED_SYSPROP = "sensitive.data.masking.enabled";

    private static final String SENSITIVE_DATA_KEYS_ENV = "SENSITIVE_DATA_KEYS";
    private static final String SENSITIVE_DATA_KEYS_SYSPROP = "sensitive.data.keys";

    /**
     * @return true if console audit logging is enabled
     */
    public boolean isAuditConsoleEnabled() {
        return Boolean.parseBoolean(getConfigurationProperty(AUDIT_CONSOLE_ENABLED_ENV, AUDIT_CONSOLE_ENABLED_SYSPROP, "false"));
    }

    /**
     * @return true if file audit logging is enabled
     */
    public boolean isAuditFileEnabled() {
        return Boolean.parseBoolean(getConfigurationProperty(AUDIT_FILE_ENABLED_ENV, AUDIT_FILE_ENABLED_SYSPROP, "false"));
    }

    /**
     * @return true if database audit logging is enabled
     */
    public boolean isAuditDatabaseEnabled() {
        return Boolean.parseBoolean(getConfigurationProperty(AUDIT_DATABASE_ENABLED_ENV, AUDIT_DATABASE_ENABLED_SYSPROP, "false"));
    }

    /**
     * @return the file path for audit logging
     */
    public String getAuditFilePath() {
        return getConfigurationProperty(AUDIT_FILE_PATH_ENV, AUDIT_FILE_PATH_SYSPROP, "./logs");
    }

    /**
     * @return the file prefix for audit logging
     */
    public String getAuditFilePrefix() {
        return getConfigurationProperty(AUDIT_FILE_PREFIX_ENV, AUDIT_FILE_PREFIX_SYSPROP, "audit-log-");
    }

    /**
     * @return the layout template for audit logging
     */
    public String getAuditLayoutTemplate() {
        // Check environment variable first
        String value = System.getenv(AUDIT_LAYOUT_TEMPLATE_ENV);
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }
        
        // Check system property without Spring property resolution to avoid placeholder issues
        try {
            value = System.getProperty(AUDIT_LAYOUT_TEMPLATE_SYSPROP);
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        } catch (Exception e) {
            logger.debug("Could not get system property {}: {}", AUDIT_LAYOUT_TEMPLATE_SYSPROP, e.getMessage());
        }
        
        // Return default template with escaped placeholders
        return "${eventDate}|${uuid}|actor=${actor}|${action}|origin=${origin} => ${foreach fields field}${field.name} ${field.type}:${field.value}, ${end}";
    }

    /**
     * @return the database URL for audit logging
     */
    public String getAuditDatabaseUrl() {
        return getConfigurationProperty(AUDIT_DATABASE_URL_ENV, AUDIT_DATABASE_URL_SYSPROP, "jdbc:postgresql://localhost:5432/demo");
    }

    /**
     * @return the database username for audit logging
     */
    public String getAuditDatabaseUsername() {
        return getConfigurationProperty(AUDIT_DATABASE_USERNAME_ENV, AUDIT_DATABASE_USERNAME_SYSPROP, "");
    }

    /**
     * @return the database password for audit logging
     */
    public String getAuditDatabasePassword() {
        return getConfigurationProperty(AUDIT_DATABASE_PASSWORD_ENV, AUDIT_DATABASE_PASSWORD_SYSPROP, "");
    }

    /**
     * @return true if secure layout is enabled
     */
    public boolean isSecureLayoutEnabled() {
        return Boolean.parseBoolean(getConfigurationProperty(SECURE_LAYOUT_ENABLED_ENV, SECURE_LAYOUT_ENABLED_SYSPROP, "false"));
    }

    /**
     * @return the secure layout key for encryption
     */
    public String getSecureLayoutKey() {
        return getConfigurationProperty(SECURE_LAYOUT_KEY_ENV, SECURE_LAYOUT_KEY_SYSPROP, "");
    }

    /**
     * @return the secure layout salt for encryption
     */
    public String getSecureLayoutSalt() {
        return getConfigurationProperty(SECURE_LAYOUT_SALT_ENV, SECURE_LAYOUT_SALT_SYSPROP, "");
    }

    /**
     * @return true if encryption is enabled
     */
    public boolean isEncryptionEnabled() {
        return Boolean.parseBoolean(getConfigurationProperty(ENCRYPTION_SECRET_ENABLED_ENV, ENCRYPTION_SECRET_ENABLED_SYSPROP, "false"));
    }

    /**
     * @return the secret key for encryption
     */
    public String getEncryptionSecretKey() {
        return getConfigurationProperty(ENCRYPTION_SECRET_KEY_ENV, ENCRYPTION_SECRET_KEY_SYSPROP, "");
    }

    /**
     * @return the package for API audit logging
     */
    public String getAuditApiPackage() {
        return getConfigurationProperty(AUDIT_API_PACKAGE_ENV, AUDIT_API_PACKAGE_SYSPROP, "");
    }

    /**
     * @return true if audit api is disabled
     */
    public boolean isAuditApiDisabled() {
        return Boolean.parseBoolean(getConfigurationProperty(AUDIT_API_DISABLED_ENV, AUDIT_API_DISABLED_SYSPROP, "false"));
    }

    /**
     * @return path for audit api excluded
     */
    public String getExcludedApiPath() {
        return getConfigurationProperty(EXCLUDE_API_PATH_ENV, EXCLUDE_API_PATH_SYSPROP, "/");
    }

    /**
     * @return true if audit entity is disabled
     */
    public boolean isAuditEntityDisabled() {
        return Boolean.parseBoolean(getConfigurationProperty(AUDIT_ENTITY_DISABLED_ENV, AUDIT_ENTITY_DISABLED_SYSPROP, "false"));
    }

    /**
     * @return true if audit security is disabled
     */
    public boolean isAuditSecurityDisabled() {
        return Boolean.parseBoolean(getConfigurationProperty(AUDIT_SECURITY_DISABLED_ENV, AUDIT_SECURITY_DISABLED_SYSPROP, "false"));
    }

    /**
     * @return the default offset for fastCode
     */
    public String getFastCodeOffsetDefault() {
        return getConfigurationProperty(FASTCODE_OFFSET_DEFAULT_ENV, FASTCODE_OFFSET_DEFAULT_SYSPROP, "0");
    }

    /**
     * @return the default limit for fastCode
     */
    public String getFastCodeLimitDefault() {
        return getConfigurationProperty(FASTCODE_LIMIT_DEFAULT_ENV, FASTCODE_LIMIT_DEFAULT_SYSPROP, "10");
    }

    /**
     * @return the default sort direction for fastCode
     */
    public String getFastCodeSortDirectionDefault() {
        return getConfigurationProperty(FASTCODE_SORT_DIRECTION_DEFAULT_ENV, FASTCODE_SORT_DIRECTION_DEFAULT_SYSPROP, "ASC");
    }

    /**
     * @return the Audit Auth Header Name
     */
    public String getAuditAuthHeaderName() {
        return getConfigurationProperty(AUDIT_AUTH_HEADER_NAME_ENV, AUDIT_AUTH_HEADER_NAME_SYSPROP, "Authorization");
    }

    /**
     * @return the Audit Jwt Secret Key
     */
    public String getAuditJwtSecretKey() {
        return getConfigurationProperty(AUDIT_JWT_SECRET_KEY_ENV, AUDIT_JWT_SECRET_KEY_SYSPROP, "ApplicationSecureSecretKeyToGenerateJWTsTokenForAuthenticationAndAuthorization");
    }

    /**
     * @return the OIDC issuer URI
     */
    public String getOidcIssuerUri() {
        return getConfigurationProperty(OIDC_ISSUER_URI_ENV, OIDC_ISSUER_URI_SYSPROP, "https://default-issuer.com/oauth2/default");
    }

    /**
     * @return true if sensitive data masking is enabled
     */
    public boolean isSensitiveDataMaskingEnabled() {
        return Boolean.parseBoolean(getConfigurationProperty(SENSITIVE_DATA_MASKING_ENABLED_ENV, SENSITIVE_DATA_MASKING_ENABLED_SYSPROP, "false"));
    }

    /**
     * @return the sensitive data keys configuration as a map
     */
    public Map<String, String> getSensitiveDataKeys() {
        String keysConfig = getConfigurationProperty(SENSITIVE_DATA_KEYS_ENV, SENSITIVE_DATA_KEYS_SYSPROP, "");
        if (keysConfig == null || keysConfig.trim().isEmpty()) {
            return getDefaultSensitiveDataKeys();
        }
        
        try {
            Map<String, String> keys = new java.util.HashMap<>();
            String[] pairs = keysConfig.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    keys.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
            return keys;
        } catch (Exception e) {
            logger.warn("Failed to parse sensitive data keys, using defaults: {}", e.getMessage());
            return getDefaultSensitiveDataKeys();
        }
    }

    /**
     * @return default sensitive data keys
     */
    private Map<String, String> getDefaultSensitiveDataKeys() {
        Map<String, String> defaultKeys = new java.util.HashMap<>();
        defaultKeys.put("password", "(?i)(\"?(password|pwd|secret|pass|key|token|apikey|api_key)\"?)" +
                "\\s*[:=,;\\-\\>]\\s*([\"']?)[^\\s\"'}]+\\2?");
        defaultKeys.put("creditcard", "\\b(?:\\d[ -]*?){13,16}\\b");
        defaultKeys.put("ssn", "\\b\\d{3}-?\\d{2}-?\\d{4}\\b");
        defaultKeys.put("email", "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
        return defaultKeys;
    }
    /**
     * Looks for the given key in the following places (in order):
     *
     * 1) Environment variables
     * 2) System Properties
     *
     * @param envKey
     * @param sysPropKey
     * @param defaultValue
     * @return the configured property value or default value if not found
     */
    private String getConfigurationProperty(String envKey, String sysPropKey, String defaultValue) {
        String value = env.getProperty(sysPropKey);
        if (value == null || value.trim().isEmpty()) {
            value = System.getenv(envKey);
        }
        if (value == null || value.trim().isEmpty()) {
            value = defaultValue;
        }
        logger.debug("Config Property: {}/{} = {}", envKey, sysPropKey, value);
        return value;
    }
}