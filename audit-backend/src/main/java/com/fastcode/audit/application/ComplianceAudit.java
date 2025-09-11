package com.fastcode.audit.application;

import com.fastcode.audit.AuditPropertiesConfiguration;
import com.fastcode.audit.application.AuditField;
import com.fastcode.audit.utils.PrivacyAwareUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * compliance audit service to replace audit4j ComplianceAudit
 * Provides compliance-specific auditing capabilities
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ComplianceAudit {

    private final AuditService customAuditService;
    private final AuditPropertiesConfiguration auditConfig;

    /**
     * Log GDPR compliance event
     */
    public void logGdprEvent(String action, String dataSubject, String dataType, String actor, String origin) {
        Map<String, Object> details = new HashMap<>();
        details.put("dataSubject", dataSubject);
        details.put("dataType", dataType);
        details.put("gdprArticle", "Article 6");
        details.put("lawfulBasis", "Consent");
        details.put("retentionPeriod", "7 years");

        customAuditService.logComplianceAudit(
                action,
                actor,
                origin,
                "GDPR",
                details
        );
    }

    /**
     * Log data retention event
     */
    public void logDataRetentionEvent(String dataType, String retentionPeriod, String actor, String origin) {
        Map<String, Object> details = new HashMap<>();
        details.put("dataType", dataType);
        details.put("retentionPeriod", retentionPeriod);
        details.put("action", "DATA_RETENTION_CHECK");

        customAuditService.logComplianceAudit(
                "DATA_RETENTION",
                actor,
                origin,
                "DATA_RETENTION",
                details
        );
    }

    /**
     * Log data deletion event
     */
    public void logDataDeletionEvent(String dataSubject, String dataType, String reason, String actor, String origin) {
        Map<String, Object> details = new HashMap<>();
        details.put("dataSubject", dataSubject);
        details.put("dataType", dataType);
        details.put("deletionReason", reason);
        details.put("action", "DATA_DELETION");

        customAuditService.logComplianceAudit(
                "DATA_DELETION",
                actor,
                origin,
                "GDPR",
                details
        );
    }

    /**
     * Log consent management event
     */
    public void logConsentManagementEvent(String dataSubject, String consentType, String action, String actor, String origin) {
        Map<String, Object> details = new HashMap<>();
        details.put("dataSubject", dataSubject);
        details.put("consentType", consentType);
        details.put("action", action);
        details.put("timestamp", System.currentTimeMillis());

        customAuditService.logComplianceAudit(
                "CONSENT_MANAGEMENT",
                actor,
                origin,
                "GDPR",
                details
        );
    }

    /**
     * Log data export event
     */
    public void logDataExportEvent(String dataSubject, String dataType, String format, String actor, String origin) {
        Map<String, Object> details = new HashMap<>();
        details.put("dataSubject", dataSubject);
        details.put("dataType", dataType);
        details.put("exportFormat", format);
        details.put("action", "DATA_EXPORT");

        customAuditService.logComplianceAudit(
                "DATA_EXPORT",
                actor,
                origin,
                "GDPR",
                details
        );
    }

    /**
     * Log audit trail event
     */
    public void logAuditTrailEvent(String entityType, String entityId, String action, String actor, String origin) {
        Map<String, Object> details = new HashMap<>();
        details.put("entityType", entityType);
        details.put("entityId", entityId);
        details.put("action", action);
        details.put("auditTrail", true);

        customAuditService.logComplianceAudit(
                "AUDIT_TRAIL",
                actor,
                origin,
                "AUDIT_TRAIL",
                details
        );
    }

    /**
     * Log regulatory reporting event
     */
    public void logRegulatoryReportingEvent(String reportType, String period, String actor, String origin) {
        Map<String, Object> details = new HashMap<>();
        details.put("reportType", reportType);
        details.put("reportingPeriod", period);
        details.put("action", "REGULATORY_REPORTING");

        customAuditService.logComplianceAudit(
                "REGULATORY_REPORTING",
                actor,
                origin,
                "REGULATORY",
                details
        );
    }

    /**
     * Log policy compliance check
     */
    public void logPolicyComplianceCheck(String policyType, String entity, String result, String actor, String origin) {
        Map<String, Object> details = new HashMap<>();
        details.put("policyType", policyType);
        details.put("entity", entity);
        details.put("complianceResult", result);
        details.put("checkTimestamp", System.currentTimeMillis());

        customAuditService.logComplianceAudit(
                "POLICY_COMPLIANCE_CHECK",
                actor,
                origin,
                "POLICY_COMPLIANCE",
                details
        );
    }

    /**
     * Log data breach event
     */
    public void logDataBreachEvent(String dataType, String affectedRecords, String severity, String actor, String origin) {
        Map<String, Object> details = new HashMap<>();
        details.put("dataType", dataType);
        details.put("affectedRecords", affectedRecords);
        details.put("severity", severity);
        details.put("breachType", "DATA_BREACH");
        details.put("notificationRequired", "YES");

        customAuditService.logComplianceAudit(
                "DATA_BREACH",
                actor,
                origin,
                "GDPR",
                details
        );
    }

    /**
     * Log access control event
     */
    public void logAccessControlEvent(String resource, String accessType, String result, String actor, String origin) {
        Map<String, Object> details = new HashMap<>();
        details.put("resource", resource);
        details.put("accessType", accessType);
        details.put("accessResult", result);
        details.put("timestamp", System.currentTimeMillis());

        customAuditService.logComplianceAudit(
                "ACCESS_CONTROL",
                actor,
                origin,
                "ACCESS_CONTROL",
                details
        );
    }

    /**
     * Audit sensitive information with masking support
     * This method provides the main audit functionality as described in the usage guide
     */
    public void audit(String action, List<AuditField> fields, Map<String, String> sensitiveKeys) {
        try {
            String actor = getCurrentActor();
            String origin = "COMPLIANCE_AUDIT";
            
            Map<String, Object> details = new HashMap<>();
            
            // Process fields and apply masking if enabled
            for (AuditField field : fields) {
                String fieldValue = field.getValue();
                
                // Apply sensitive data masking if enabled
                if (auditConfig.isSensitiveDataMaskingEnabled() && sensitiveKeys != null && !sensitiveKeys.isEmpty()) {
                    fieldValue = PrivacyAwareUtils.maskSensitiveData(fieldValue, sensitiveKeys);
                }
                
                details.put(field.getName(), fieldValue);
            }
            
            // Apply encryption if enabled
            if (auditConfig.isEncryptionEnabled() && !auditConfig.getEncryptionSecretKey().isEmpty()) {
                details = PrivacyAwareUtils.encryptMap(details, auditConfig.getEncryptionSecretKey());
            }
            
            // Apply secure layout encryption if enabled
            if (auditConfig.isSecureLayoutEnabled() && !auditConfig.getSecureLayoutKey().isEmpty() && !auditConfig.getSecureLayoutSalt().isEmpty()) {
                details = PrivacyAwareUtils.encryptMapWithSecureLayout(details, auditConfig.getSecureLayoutKey(), auditConfig.getSecureLayoutSalt());
            }
            
            customAuditService.logComplianceAudit(
                    action,
                    actor,
                    origin,
                    "COMPLIANCE",
                    details
            );
            
            log.debug("Compliance audit logged: {} with {} fields", action, fields.size());
        } catch (Exception e) {
            log.error("Failed to log compliance audit: {}", e.getMessage(), e);
        }
    }


    /**
     * Get current actor from security context
     */
    private String getCurrentActor() {
        try {
            // Try to get from Spring Security context if available
            Class<?> securityContextClass = Class.forName("org.springframework.security.core.context.SecurityContextHolder");
            Object context = securityContextClass.getMethod("getContext").invoke(null);
            
            if (context != null) {
                Object authentication = context.getClass().getMethod("getAuthentication").invoke(context);
                if (authentication != null) {
                    Boolean isAuthenticated = (Boolean) authentication.getClass().getMethod("isAuthenticated").invoke(authentication);
                    if (isAuthenticated != null && isAuthenticated) {
                        String name = (String) authentication.getClass().getMethod("getName").invoke(authentication);
                        if (name != null && !"anonymousUser".equals(name)) {
                            return name;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not get actor from security context: {}", e.getMessage());
        }

        return "ANONYMOUS";
    }


}
