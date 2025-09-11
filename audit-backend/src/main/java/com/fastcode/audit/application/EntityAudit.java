package com.fastcode.audit.application;

import com.fastcode.audit.AuditPropertiesConfiguration;
import com.fastcode.audit.domain.AuditEvent;
import com.fastcode.audit.utils.PrivacyAwareUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom entity audit aspect to replace audit4j EntityAudit
 * Intercepts entity save and delete operations for auditing
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class EntityAudit {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AuditService customAuditService;

    @Autowired
    private AuditPropertiesConfiguration auditConfig;

    // Thread-local flag to prevent circular dependencies
    private static final ThreadLocal<Boolean> AUDIT_IN_PROGRESS = ThreadLocal.withInitial(() -> false);

    /**
     * Audit before entity save operations
     */
    @Before("execution(* save*(..)) && args(entity,..)")
    public void beforeSaveEntity(JoinPoint joinPoint, Object entity) {
        if (auditConfig.isAuditEntityDisabled()) {
            return;
        }

        // Skip auditing AuditEvent entities to prevent circular dependency
        if (entity instanceof AuditEvent) {
            return;
        }

        // Prevent circular dependencies using thread-local flag
        if (AUDIT_IN_PROGRESS.get()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        try {
            AUDIT_IN_PROGRESS.set(true);
            String entityName = entity.getClass().getSimpleName();
            String actor = getCurrentActor();
            Object entityId = getEntityId(entity);

            // Check if this is an update or create
            if (entityId != null) {
                Object oldEntity = entityManager.find(entity.getClass(), entityId);
                customAuditService.logEntityAudit("UPDATE", entityName, actor, entity, oldEntity);
            } else {
                customAuditService.logEntityAudit("CREATE", entityName, actor, entity, null);
            }
        } catch (Exception e) {
            log.error("Error in beforeSaveEntity audit: {}", e.getMessage(), e);
        } finally {
            AUDIT_IN_PROGRESS.set(false);
        }
    }

//    /**
//     * Audit after entity save operations
//     */
//    @AfterReturning("execution(* save*(..)) && args(entity,..)")
//    public void afterSaveEntity(JoinPoint joinPoint, Object entity) {
//        if (auditConfig.isAuditEntityDisabled()) {
//            return;
//        }
//
//        // Skip auditing AuditEvent entities to prevent circular dependency
//        if (entity instanceof AuditEvent) {
//            return;
//        }
//
//        // Prevent circular dependencies using thread-local flag
//        if (AUDIT_IN_PROGRESS.get()) {
//            return;
//        }
//
//        try {
//            AUDIT_IN_PROGRESS.set(true);
//            String actor = getCurrentActor();
//            Object entityId = getEntityId(entity);
//
//            Map<String, Object> details = new HashMap<>();
//            details.put("entityId", entityId);
//            details.put("entityClass", entity.getClass().getName());
//            details.put("operation", "SAVE_COMPLETED");
//
//            customAuditService.logAuditEvent("ENTITY_SAVE_COMPLETED", actor, "ENTITY_MANAGER", details);
//        } catch (Exception e) {
//            log.error("Error in afterSaveEntity audit: {}", e.getMessage(), e);
//        } finally {
//            AUDIT_IN_PROGRESS.set(false);
//        }
//    }

    /**
     * Audit before entity delete operations
     */
    @Before("execution(* delete*(..)) && args(entity,..)")
    public void beforeDeleteEntity(JoinPoint joinPoint, Object entity) {
        if (auditConfig.isAuditEntityDisabled()) {
            return;
        }

        // Skip auditing AuditEvent entities to prevent circular dependency
        if (entity instanceof AuditEvent) {
            return;
        }

        // Prevent circular dependencies using thread-local flag
        if (AUDIT_IN_PROGRESS.get()) {
            return;
        }

        try {
            AUDIT_IN_PROGRESS.set(true);
            String entityName = entity.getClass().getSimpleName();
            String actor = getCurrentActor();
            Object entityId = getEntityId(entity);

            Map<String, Object> details = new HashMap<>();
            details.put("entityId", entityId);
            details.put("entityClass", entity.getClass().getName());
            details.put("entityState", getEntityState(entity));

            customAuditService.logEntityAudit("DELETE", entityName, actor, entity, null);
        } catch (Exception e) {
            log.error("Error in beforeDeleteEntity audit: {}", e.getMessage(), e);
        } finally {
            AUDIT_IN_PROGRESS.set(false);
        }
    }

//    /**
//     * Audit after entity delete operations
//     */
//    @AfterReturning("execution(* delete*(..)) && args(entity,..)")
//    public void afterDeleteEntity(JoinPoint joinPoint, Object entity) {
//        if (auditConfig.isAuditEntityDisabled()) {
//            return;
//        }
//
//        // Skip auditing AuditEvent entities to prevent circular dependency
//        if (entity instanceof AuditEvent) {
//            return;
//        }
//
//        // Prevent circular dependencies using thread-local flag
//        if (AUDIT_IN_PROGRESS.get()) {
//            return;
//        }
//
//        try {
//            AUDIT_IN_PROGRESS.set(true);
//            String actor = getCurrentActor();
//            Object entityId = getEntityId(entity);
//
//            Map<String, Object> details = new HashMap<>();
//            details.put("entityId", entityId);
//            details.put("entityClass", entity.getClass().getName());
//            details.put("operation", "DELETE_COMPLETED");
//
//            customAuditService.logAuditEvent("ENTITY_DELETE_COMPLETED", actor, "ENTITY_MANAGER", details);
//        } catch (Exception e) {
//            log.error("Error in afterDeleteEntity audit: {}", e.getMessage(), e);
//        } finally {
//            AUDIT_IN_PROGRESS.set(false);
//        }
//    }

    /**
     * Get the current actor (user) from security context or session
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

        // Fallback to system user
        return "SYSTEM";
    }

    /**
     * Get entity ID for comparison
     */
    private Object getEntityId(Object entity) {
        if (entity == null) {
            return null;
        }

        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            return idField.get(entity);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Get entity state as string representation
     */
    private String getEntityState(Object entity) {
        if (entity == null) {
            return "null";
        }

        StringBuilder state = new StringBuilder();
        try {
            Field[] fields = entity.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    Object value = field.get(entity);
                    String valueStr = value != null ? value.toString() : "null";
                    
                    // Apply sensitive data masking if enabled
                    if (auditConfig.isSensitiveDataMaskingEnabled()) {
                        valueStr = PrivacyAwareUtils.maskSensitiveData(valueStr, auditConfig.getSensitiveDataKeys());
                    }
                    
                    if (state.length() > 0) {
                        state.append(", ");
                    }
                    state.append(field.getName()).append(": ").append(valueStr);
                } catch (IllegalAccessException e) {
                    if (state.length() > 0) {
                        state.append(", ");
                    }
                    state.append(field.getName()).append(": N/A");
                }
            }
        } catch (Exception e) {
            return "Error getting entity state: " + e.getMessage();
        }
        return state.toString();
    }
}
