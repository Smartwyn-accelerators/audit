package com.fastcode.audit.application;

import com.fastcode.audit.AuditPropertiesConfiguration;
import com.fastcode.audit.domain.AuditEvent;
import com.fastcode.audit.domain.QAuditEvent;
import com.fastcode.audit.domain.IAuditRepository;
import com.fastcode.audit.handler.AuditHandlerManager;
import com.fastcode.audit.performance.AuditPerformanceInterceptor;
import com.fastcode.audit.search.SearchCriteria;
import com.fastcode.audit.search.SearchFields;
import com.fastcode.audit.search.SearchUtils;
import com.fastcode.audit.utils.PrivacyAwareUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditService {

    @Qualifier("auditRepository")
    @NonNull protected final IAuditRepository _auditRepository;

    @NonNull protected final ObjectMapper objectMapper;

    @NonNull protected final AuditPropertiesConfiguration auditConfig;
    
    @NonNull protected final AuditHandlerManager handlerManager;
    
    @NonNull protected final AuditPerformanceInterceptor performanceInterceptor;


    public List<AuditEventDto> findAll(SearchCriteria search, Pageable pageable) throws MalformedURLException {
        Page<AuditEvent> res = _auditRepository.findAll(search(search), pageable);
        return res.getContent().stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<String> findAllAction() {
        return  _auditRepository.findAllAction();
    }

    public BooleanBuilder search(SearchCriteria search) throws MalformedURLException {

        QAuditEvent auditEvent = QAuditEvent.auditEvent;

        if (search != null) {
            Map<String, SearchFields> map = new HashMap<>();
            for (SearchFields fieldDetails : search.getFields()) {
                map.put(fieldDetails.getFieldName(), fieldDetails);
            }
            List<String> keysList = new ArrayList<String>(map.keySet());
            checkProperties(keysList);
            return searchKeyValuePair(auditEvent, map, search.getJoinColumns());
        }
        return null;
    }

    public void checkProperties(List<String> list) throws MalformedURLException {
        for (int i = 0; i < list.size(); i++) {
            if (!(list.get(i).replace("%20", "").trim().equals("actor") ||
                    list.get(i).replace("%20", "").trim().equals("action") ||
                    list.get(i).replace("%20", "").trim().equals("origin") ||
                    list.get(i).replace("%20", "").trim().equals("path") ||
                    list.get(i).replace("%20", "").trim().equals("httpMethod") ||
                    list.get(i).replace("%20", "").trim().equals("eventTime") ||
                    list.get(i).replace("%20", "").trim().equals("entityName") ||
                    list.get(i).replace("%20", "").trim().equals("operation") ||
                    list.get(i).replace("%20", "").trim().equals("responseStatus") ||
                    list.get(i).replace("%20", "").trim().equals("exceptionType") ||
                    list.get(i).replace("%20", "").trim().equals("sessionId") ||
                    list.get(i).replace("%20", "").trim().equals("userId") ||
                    list.get(i).replace("%20", "").trim().equals("username") ||
                    list.get(i).replace("%20", "").trim().equals("securityEvent") ||
                    list.get(i).replace("%20", "").trim().equals("complianceEvent") ||
                    list.get(i).replace("%20", "").trim().equals("errorEvent"))) {
                throw new MalformedURLException("Wrong URL Format: Property " + list.get(i) + " not found!");
            }
        }
    }

    public static Timestamp stringToTimestamp(String dateString) {
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME);
            return Timestamp.valueOf(localDateTime);
        } catch (Exception e) {
            return null; // Handle invalid date strings
        }
    }


    public BooleanBuilder searchKeyValuePair(QAuditEvent auditEvent, Map<String, SearchFields> map, Map<String, String> joinColumns) {
        BooleanBuilder builder = new BooleanBuilder();

        for (Map.Entry<String, SearchFields> details : map.entrySet()) {

            if (details.getKey().replace("%20", "").trim().equals("actor")) {
                builder.and(auditEvent.actor.likeIgnoreCase("%" + details.getValue().getSearchValue() + "%"));

            }
            if (details.getKey().replace("%20", "").trim().equals("action")) {
                builder.and(auditEvent.action.likeIgnoreCase("%" + details.getValue().getSearchValue() + "%"));

            }
            if (details.getKey().replace("%20", "").trim().equals("origin")) {
                builder.and(auditEvent.origin.likeIgnoreCase("%" + details.getValue().getSearchValue() + "%"));
            }
            // Search using dedicated columns for better performance
            if (details.getKey().replace("%20", "").trim().equals("httpMethod")) {
                builder.and(auditEvent.httpMethod.likeIgnoreCase("%" + details.getValue().getSearchValue() + "%"));
            }

            if (details.getKey().replace("%20", "").trim().equals("path")) {
                builder.and(auditEvent.path.likeIgnoreCase("%" + details.getValue().getSearchValue() + "%"));
            }
            if (details.getKey().replace("%20", "").trim().equals("entityName")) {
                builder.and(auditEvent.entityName.likeIgnoreCase("%" + details.getValue().getSearchValue() + "%"));
            }
            if (details.getKey().replace("%20", "").trim().equals("operation")) {
                builder.and(auditEvent.operation.likeIgnoreCase("%" + details.getValue().getSearchValue() + "%"));
            }
            if (details.getKey().replace("%20", "").trim().equals("responseStatus")) {
                builder.and(auditEvent.responseStatus.likeIgnoreCase("%" + details.getValue().getSearchValue() + "%"));
            }
            if (details.getKey().replace("%20", "").trim().equals("exceptionType")) {
                builder.and(auditEvent.exceptionType.likeIgnoreCase("%" + details.getValue().getSearchValue() + "%"));
            }
            
            // Additional JSON field searches for other fields
            if (details.getKey().replace("%20", "").trim().equals("sessionId")) {
                StringTemplate jsonValue = Expressions.stringTemplate("CAST({0}->>'sessionId' AS TEXT)", auditEvent.elements);
                builder.and(jsonValue.likeIgnoreCase("%" + details.getValue().getSearchValue() + "%"));
            }
            if (details.getKey().replace("%20", "").trim().equals("userId")) {
                StringTemplate jsonValue = Expressions.stringTemplate("CAST({0}->>'userId' AS TEXT)", auditEvent.elements);
                builder.and(jsonValue.likeIgnoreCase("%" + details.getValue().getSearchValue() + "%"));
            }
            if (details.getKey().replace("%20", "").trim().equals("username")) {
                StringTemplate jsonValue = Expressions.stringTemplate("CAST({0}->>'username' AS TEXT)", auditEvent.elements);
                builder.and(jsonValue.likeIgnoreCase("%" + details.getValue().getSearchValue() + "%"));
            }
            
            // Event type searches (exists operator)
            if (details.getKey().replace("%20", "").trim().equals("securityEvent")) {
                StringTemplate jsonValue = Expressions.stringTemplate("{0}->'securityEvent'", auditEvent.elements);
                if (details.getValue().getOperator().equals("exists")) {
                    builder.and(jsonValue.isNotNull());
                } else {
                    StringTemplate jsonValueStr = Expressions.stringTemplate("CAST({0}->>'securityEvent' AS TEXT)", auditEvent.elements);
                    builder.and(jsonValueStr.likeIgnoreCase("%" + details.getValue().getSearchValue() + "%"));
                }
            }
            if (details.getKey().replace("%20", "").trim().equals("complianceEvent")) {
                StringTemplate jsonValue = Expressions.stringTemplate("{0}->'complianceEvent'", auditEvent.elements);
                if (details.getValue().getOperator().equals("exists")) {
                    builder.and(jsonValue.isNotNull());
                } else {
                    StringTemplate jsonValueStr = Expressions.stringTemplate("CAST({0}->>'complianceEvent' AS TEXT)", auditEvent.elements);
                    builder.and(jsonValueStr.likeIgnoreCase("%" + details.getValue().getSearchValue() + "%"));
                }
            }
            if (details.getKey().replace("%20", "").trim().equals("errorEvent")) {
                StringTemplate jsonValue = Expressions.stringTemplate("{0}->'errorEvent'", auditEvent.elements);
                if (details.getValue().getOperator().equals("exists")) {
                    builder.and(jsonValue.isNotNull());
                } else {
                    StringTemplate jsonValueStr = Expressions.stringTemplate("CAST({0}->>'errorEvent' AS TEXT)", auditEvent.elements);
                    builder.and(jsonValueStr.likeIgnoreCase("%" + details.getValue().getSearchValue() + "%"));
                }
            }
//
//            if (details.getKey().replace("%20", "").trim().equals("eventTime")) {
//                String eventTimeStr = details.getValue().getSearchValue();
//                LocalDateTime eventTime = getDateFromDateString(eventTimeStr);
//                builder.and(auditEvent.timestamp.gt(Timestamp.valueOf(eventTime))); // Use 'gt' (greater than) for "after"
//            }

            if (details.getKey().replace("%20", "").trim().equals("eventTime")) {
                if (details.getValue().getOperator().equals("equals") && SearchUtils.stringToTimestamp(details.getValue().getSearchValue()) != null) {
                    builder.and(auditEvent.timestamp.eq(SearchUtils.stringToTimestamp(details.getValue().getSearchValue())));
                } else if (details.getValue().getOperator().equals("notEqual") && SearchUtils.stringToTimestamp(details.getValue().getSearchValue()) != null) {
                    builder.and(auditEvent.timestamp.ne(SearchUtils.stringToTimestamp(details.getValue().getSearchValue())));
                } else if (details.getValue().getOperator().equals("range")) {
                    Timestamp startTimestamp = SearchUtils.stringToTimestamp(details.getValue().getStartingValue());
                    Timestamp endTimestamp = SearchUtils.stringToTimestamp(details.getValue().getEndingValue());
                    if (startTimestamp != null && endTimestamp != null) {
                        builder.and(auditEvent.timestamp.between(startTimestamp, endTimestamp));
                    } else if (endTimestamp != null) {
                        builder.and(auditEvent.timestamp.loe(endTimestamp));
                    } else if (startTimestamp != null) {
                        builder.and(auditEvent.timestamp.goe(startTimestamp));
                    }
                }
            }
        }
        return builder;
    }


    private AuditEventDto toDto(AuditEvent auditEvent) {
        if (auditEvent == null) {
            return null;
        }
        AuditEventDto dto = new AuditEventDto();
        dto.setIdentifier(auditEvent.getIdentifier());
        dto.setTimestamp(auditEvent.getTimestamp());
        dto.setActor(auditEvent.getActor());
        dto.setOrigin(auditEvent.getOrigin());
        dto.setAction(auditEvent.getAction());

        List<AuditField> fields = parseFieldsFromMap(auditEvent.getElements());
        fields.add(new AuditField("httpMethod", auditEvent.getHttpMethod(), "String"));
        fields.add(new AuditField("path", auditEvent.getPath(), "String"));
        fields.add(new AuditField("entityName", auditEvent.getEntityName(), "String"));
        fields.add(new AuditField("operation", auditEvent.getOperation(), "String"));
        fields.add(new AuditField("responseStatus", auditEvent.getResponseStatus(), "String"));
        fields.add(new AuditField("exceptionType", auditEvent.getExceptionType(), "String"));
        dto.setElements(fields);
        return dto;
    }

    public List<AuditField> parseFieldsFromMap(Map<String, Object> elementsMap) {
        List<AuditField> fieldList = new ArrayList<>();

        if (elementsMap != null && !elementsMap.isEmpty()) {
            for (Map.Entry<String, Object> entry : elementsMap.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue() != null ? entry.getValue().toString() : "null";
                String type = entry.getValue() != null ? entry.getValue().getClass().getSimpleName() : "null";
                fieldList.add(new AuditField(name, value, type));
            }
        }

        return fieldList;
    }

    public void logAudit(AuditInput input) {
        Map<String, Object> details = new HashMap<>();
        details.put("sessionId", input.getSessionId());
        input.getDetails().forEach(details::put);
        
        logAuditEvent(
            input.getAction(),
            input.getUserId(),
            "CUSTOM",
            details
        );
    }


    /**
     * Log a custom audit event
     */
    @Transactional
    public void logAuditEvent(String action, String actor, String origin, Map<String, Object> details) {
        if (auditConfig.isAuditEntityDisabled()) {
            return;
        }

        try {
            // Apply sensitive data masking if enabled
            Map<String, Object> processedDetails = details;
            if (auditConfig.isSensitiveDataMaskingEnabled()) {
                processedDetails = PrivacyAwareUtils.maskMap(details, auditConfig.getSensitiveDataKeys());
            }

            // Apply encryption if enabled
            if (auditConfig.isEncryptionEnabled() && !auditConfig.getEncryptionSecretKey().isEmpty()) {
                processedDetails = PrivacyAwareUtils.encryptMap(processedDetails, auditConfig.getEncryptionSecretKey());
            }

            // Apply secure layout encryption if enabled
            if (auditConfig.isSecureLayoutEnabled() && !auditConfig.getSecureLayoutKey().isEmpty() && !auditConfig.getSecureLayoutSalt().isEmpty()) {
                processedDetails = PrivacyAwareUtils.encryptMapWithSecureLayout(processedDetails, auditConfig.getSecureLayoutKey(), auditConfig.getSecureLayoutSalt());
            }

            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setIdentifier(UUID.randomUUID().toString());
            auditEvent.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));
            auditEvent.setAction(action);
            auditEvent.setActor(actor);
            auditEvent.setOrigin(origin);

            auditEvent.setHttpMethod(getStringValue(processedDetails, "httpMethod"));
            auditEvent.setPath(getStringValue(processedDetails, "path"));
            auditEvent.setEntityName(getStringValue(processedDetails, "entityName"));
            auditEvent.setOperation(getStringValue(processedDetails, "operation"));
            auditEvent.setResponseStatus(getStringValue(processedDetails, "responseStatus"));
            auditEvent.setExceptionType(getStringValue(processedDetails, "exceptionType"));

            auditEvent.setElements(processedDetails);

            // Process through performance-optimized interceptor
            performanceInterceptor.processAuditEvent(auditEvent);
            log.debug("Audit event logged: {} by {}", action, actor);
        } catch (Exception e) {
            log.error("Failed to log audit event: {}", e.getMessage(), e);
        }
    }

    /**
     * Helper method to safely extract string values from details map
     */
    private String getStringValue(Map<String, Object> details, String key) {
        Object value = details.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Log entity audit event (CREATE, UPDATE, DELETE)
     */
    @Transactional
    public void logEntityAudit(String operation, String entityName, String actor, Object entity, Object oldEntity) {
        if (auditConfig.isAuditEntityDisabled()) {
            return;
        }

        try {
            Map<String, Object> details = new HashMap<>();
            details.put("entityName", entityName);
            details.put("operation", operation);
            details.put("description", operation + " entity: " + entityName);

            if (oldEntity != null) {
                details.put("old_value", getEntityState(oldEntity));
            }
            if (entity != null) {
                details.put("new_value", getEntityState(entity));
            }

            String action = "ENTITY_" + operation;
            logAuditEvent(action, actor, "ENTITY_MANAGER", details);
        } catch (Exception e) {
            log.error("Failed to log entity audit: {}", e.getMessage(), e);
        }
    }

    /**
     * Log API audit event
     */
    @Transactional
    public void logApiAudit(String httpMethod, String path, String actor, String origin,
                            String responseStatus, String contentType, String userAgent, String action) {
        if (auditConfig.isAuditApiDisabled()) {
            return;
        }

        try {
            Map<String, Object> details = new HashMap<>();
            details.put("httpMethod", httpMethod);
            details.put("path", path);
            details.put("responseStatus", responseStatus);
            details.put("contentType", contentType);
            details.put("browser", userAgent);

            logAuditEvent(action, actor, origin, details);
        } catch (Exception e) {
            log.error("Failed to log API audit: {}", e.getMessage(), e);
        }
    }

    /**
     * Log security audit event
     */
    @Transactional
    public void logSecurityAudit(String action, String actor, String origin, String description, Map<String, Object> details) {
        if (auditConfig.isAuditEntityDisabled()) {
            return;
        }

        try {
            Map<String, Object> securityDetails = new HashMap<>(details);
            securityDetails.put("description", description);
            securityDetails.put("securityEvent", true);

            logAuditEvent("SECURITY_" + action, actor, origin, securityDetails);
        } catch (Exception e) {
            log.error("Failed to log security audit: {}", e.getMessage(), e);
        }
    }

    /**
     * Log compliance audit event
     */
    @Transactional
    public void logComplianceAudit(String action, String actor, String origin, String complianceType, Map<String, Object> details) {
        if (auditConfig.isAuditEntityDisabled()) {
            return;
        }

        try {
            Map<String, Object> complianceDetails = new HashMap<>(details);
            complianceDetails.put("complianceType", complianceType);
            complianceDetails.put("complianceEvent", true);

            logAuditEvent("COMPLIANCE_" + action, actor, origin, complianceDetails);
        } catch (Exception e) {
            log.error("Failed to log compliance audit: {}", e.getMessage(), e);
        }
    }

    /**
     * Log error audit event
     */
    @Transactional
    public void logErrorAudit(String action, String actor, String origin, String errorType, String errorMessage, Map<String, Object> details) {
        if (auditConfig.isAuditEntityDisabled()) {
            return;
        }

        try {
            Map<String, Object> errorDetails = new HashMap<>(details);
            errorDetails.put("exceptionType", errorType);
            errorDetails.put("message", errorMessage);
            errorDetails.put("errorEvent", true);

            logAuditEvent("ERROR_" + action, actor, origin, errorDetails);
        } catch (Exception e) {
            log.error("Failed to log error audit: {}", e.getMessage(), e);
        }
    }


    /**
     * Convert object to string representation
     */
    private String convertToString(Object value) {
        if (value == null) {
            return "null";
        }

        // Handle different types
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        } else if (value instanceof java.util.Date) {
            return value.toString();
        } else if (value instanceof java.time.temporal.Temporal) {
            return value.toString();
        } else if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            if (collection.isEmpty()) {
                return "[]";
            }
            StringBuilder sb = new StringBuilder("[");
            int count = 0;
            for (Object item : collection) {
                if (count > 0) sb.append(", ");
                sb.append(convertToString(item));
                count++;
            }
            sb.append("]");
            return sb.toString();
        } else if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            if (map.isEmpty()) {
                return "{}";
            }
            StringBuilder sb = new StringBuilder("{");
            int count = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (count > 0) sb.append(", ");
                sb.append(convertToString(entry.getKey())).append(": ").append(convertToString(entry.getValue()));
                count++;
            }
            sb.append("}");
            return sb.toString();
        } else {
            return value.toString();
        }
    }

    /**
     * Create simple JSON representation
     */
    private String createSimpleJson(Map<String, Object> details) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;

        for (Map.Entry<String, Object> entry : details.entrySet()) {
            if (!first) {
                sb.append(", ");
            }

            String key = entry.getKey();
            String value = entry.getValue() != null ? entry.getValue().toString() : "null";

            // Escape quotes in values
            value = value.replace("\"", "\\\"");
            sb.append("\"").append(key).append("\":\"").append(value).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
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
            java.lang.reflect.Field[] fields = entity.getClass().getDeclaredFields();

            for (int i = 0; i < fields.length; i++) {
                java.lang.reflect.Field field = fields[i];
                field.setAccessible(true);
                try {
                    Object value = field.get(entity);
                    String fieldValue = convertToString(value);

                    if (i > 0) {
                        state.append(", ");
                    }
                    state.append(field.getName()).append(": ").append(fieldValue);
                } catch (IllegalAccessException e) {
                    if (i > 0) {
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

