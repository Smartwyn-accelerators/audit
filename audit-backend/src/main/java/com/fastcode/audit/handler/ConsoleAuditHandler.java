package com.fastcode.audit.handler;

import com.fastcode.audit.AuditPropertiesConfiguration;
import com.fastcode.audit.domain.AuditEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Console audit handler for logging audit events to console
 * Similar to audit4j console handler but optimized for performance
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConsoleAuditHandler implements AuditHandler {

    private final AuditPropertiesConfiguration auditConfig;
    private final Executor asyncExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "audit-console-handler");
        t.setDaemon(true);
        return t;
    });

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public void handle(AuditEvent auditEvent) {
        if (!auditConfig.isAuditConsoleEnabled()) {
            return;
        }

        // Process asynchronously to avoid blocking the main thread
        CompletableFuture.runAsync(() -> {
            try {
                String formattedMessage = formatAuditEvent(auditEvent);
                System.out.println(formattedMessage);
            } catch (Exception e) {
                log.error("Error in console audit handler: {}", e.getMessage(), e);
            }
        }, asyncExecutor);
    }

    @Override
    public boolean isEnabled() {
        return auditConfig.isAuditConsoleEnabled();
    }

    /**
     * Format audit event for console output
     */
    private String formatAuditEvent(AuditEvent auditEvent) {
        StringBuilder sb = new StringBuilder();
        
        // Basic event information
        sb.append("[").append(auditEvent.getTimestamp().toLocalDateTime().format(DATE_FORMATTER)).append("] ");
        sb.append("AUDIT: ").append(auditEvent.getAction());
        sb.append(" | Actor: ").append(auditEvent.getActor());
        sb.append(" | Origin: ").append(auditEvent.getOrigin());
        sb.append(" | ID: ").append(auditEvent.getIdentifier());

        // Add HTTP method and path if available
        if (auditEvent.getHttpMethod() != null) {
            sb.append(" | Method: ").append(auditEvent.getHttpMethod());
        }
        if (auditEvent.getPath() != null) {
            sb.append(" | Path: ").append(auditEvent.getPath());
        }

        // Add entity information if available
        if (auditEvent.getEntityName() != null) {
            sb.append(" | Entity: ").append(auditEvent.getEntityName());
        }
        if (auditEvent.getOperation() != null) {
            sb.append(" | Operation: ").append(auditEvent.getOperation());
        }

        // Add response status if available
        if (auditEvent.getResponseStatus() != null) {
            sb.append(" | Status: ").append(auditEvent.getResponseStatus());
        }

        // Add exception type if available
        if (auditEvent.getExceptionType() != null) {
            sb.append(" | Exception: ").append(auditEvent.getExceptionType());
        }

        // Add elements if available
        if (auditEvent.getElements() != null && !auditEvent.getElements().isEmpty()) {
            sb.append(" | Details: ");
            formatElements(sb, auditEvent.getElements(), 0);
        }

        return sb.toString();
    }

    /**
     * Format elements map for console output
     */
    private void formatElements(StringBuilder sb, Map<String, Object> elements, int depth) {
        if (depth > 3) { // Prevent deep recursion
            sb.append("{...}");
            return;
        }

        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : elements.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            first = false;

            sb.append(entry.getKey()).append("=");
            Object value = entry.getValue();
            
            if (value == null) {
                sb.append("null");
            } else if (value instanceof String) {
                String strValue = (String) value;
                // Truncate very long strings for console output
                if (strValue.length() > 100) {
                    sb.append("\"").append(strValue.substring(0, 97)).append("...\"");
                } else {
                    sb.append("\"").append(strValue).append("\"");
                }
            } else if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapValue = (Map<String, Object>) value;
                formatElements(sb, mapValue, depth + 1);
            } else {
                sb.append(value.toString());
            }
        }
        sb.append("}");
    }

    @Override
    public void shutdown() {
        // No specific shutdown needed for console handler
        log.debug("Console audit handler shutdown");
    }
}
