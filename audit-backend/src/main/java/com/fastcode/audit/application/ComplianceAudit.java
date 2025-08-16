package com.fastcode.audit.application;

import com.fastcode.audit.utils.PrivacyAwareUtils;
import org.audit4j.core.AuditManager;
import org.audit4j.core.dto.AuditEvent;
import org.audit4j.core.dto.Field;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ComplianceAudit {

    public void audit(String action, List<Field> fields) {
        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setAction(action);
        auditEvent.setFields(fields);
        AuditManager.getInstance().audit(auditEvent);
    }

    public void audit(String action, List<Field> fields, Map<String, String> sensitiveKeys) {
        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setAction(action);
        ArrayList<Field> newFields = new ArrayList<>();
        for (Field field: fields){
            String updatedValue = PrivacyAwareUtils.maskSensitiveData(field.getValue(), sensitiveKeys);
            newFields.add(new Field(field.getName(), updatedValue, field.getType()));
        }
        auditEvent.setFields(newFields);
        AuditManager.getInstance().audit(auditEvent);
    }
}
