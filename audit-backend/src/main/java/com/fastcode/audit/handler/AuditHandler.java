package com.fastcode.audit.handler;

import com.fastcode.audit.domain.AuditEvent;

/**
 * Base interface for audit handlers
 * Similar to audit4j handler interface but simplified for our needs
 */
public interface AuditHandler {

    /**
     * Handle an audit event
     * @param auditEvent the audit event to handle
     */
    void handle(AuditEvent auditEvent);

    /**
     * Check if this handler is enabled
     * @return true if enabled, false otherwise
     */
    boolean isEnabled();

    /**
     * Shutdown the handler
     * Called when the application is shutting down
     */
    void shutdown();
}
