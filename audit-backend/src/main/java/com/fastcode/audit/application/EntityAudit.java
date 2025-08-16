package com.fastcode.audit.application;

import com.fastcode.audit.AuditPropertiesConfiguration;
import com.fastcode.audit.utils.PrivacyAwareUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.audit4j.core.AuditManager;
import org.audit4j.core.dto.AuditEvent;
import org.audit4j.core.exception.HandlerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.lang.reflect.Field;

@Aspect
@Component
public class EntityAudit {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AuditPropertiesConfiguration env;

    @Before("execution(* save*(..)) && args(entity,..)")
    public void beforeSaveEntity(Object entity) throws HandlerException {

        if (!env.isAuditEntityDisabled()) {
            AuditEvent auditEvent = new AuditEvent();

            auditEvent.addField("entityName", entity.getClass().getSimpleName());
            // Compare old and new values
            if (getEntityId(entity) != null) {
                Object oldEntity = entityManager.find(entity.getClass(), getEntityId(entity));
                String oldValues = (oldEntity != null) ? getEntityState(oldEntity) : "N/A";
                String newValues = getEntityState(entity);
                auditEvent.addField("old_value", oldValues);
                auditEvent.addField("new_value", newValues);
                auditEvent.setAction((oldEntity != null) ? "ENTITY_UPDATE" : "ENTITY_CREATE");
                auditEvent.addField("operation", (oldEntity != null) ? "UPDATE" : "CREATE");
                auditEvent.addField("description", (oldEntity != null) ? "Update entity: " : "Create entity: " + entity.getClass().getSimpleName());
            } else {
                auditEvent.addField("operation", "CREATE");
                auditEvent.setAction("ENTITY_CREATE");
                auditEvent.addField("description", "Create entity: " + entity.getClass().getSimpleName());
            }

            if (env.isEncryptionEnabled()) {
                AuditManager.getInstance().audit(PrivacyAwareUtils.encryptEvent(auditEvent, env.getEncryptionSecretKey()));
            } else {
                AuditManager.getInstance().audit(auditEvent);
            }
        }

    }

    // Helper method to extract the ID field from an entity
    private Object getEntityId(Object entity) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            return idField.get(entity);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    private String getEntityState(Object entity) {
        StringBuilder state = new StringBuilder();
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(entity);
                state.append(field.getName()).append(": ").append(value).append(", ");
            } catch (IllegalAccessException e) {
                // Handle exception
                state.append(field.getName()).append(": ").append("N/A").append(", ");
            }
        }
        return state.toString();
    }

    @Before("execution(* delete*(..)) && args(entity,..)")
    public void beforeDeleteEntity(Object entity) throws HandlerException {
        if (!env.isAuditEntityDisabled()) {
            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setAction("ENTITY_DELETE");
            auditEvent.addField("operation", "DELETE");
            auditEvent.addField("entityName", entity.getClass().getSimpleName());
            auditEvent.addField("description", "Deleted entity: " + entity.getClass().getSimpleName());
            if (env.isEncryptionEnabled()) {
                AuditManager.getInstance().audit(PrivacyAwareUtils.encryptEvent(auditEvent, env.getEncryptionSecretKey()));
            } else {
                AuditManager.getInstance().audit(auditEvent);
            }
        }
    }
}
