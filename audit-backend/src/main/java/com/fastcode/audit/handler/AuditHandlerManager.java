package com.fastcode.audit.handler;

import com.fastcode.audit.domain.AuditEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Manager for all audit handlers
 * Coordinates console, file, and database handlers based on configuration
 * Similar to audit4j handler manager but optimized for performance
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditHandlerManager {

    private final List<AuditHandler> auditHandlers;
    private final Executor asyncExecutor = Executors.newFixedThreadPool(3, r -> {
        Thread t = new Thread(r, "audit-handler-manager");
        t.setDaemon(true);
        return t;
    });

    /**
     * Handle audit event through all enabled handlers
     * This method is designed to be fast and non-blocking
     */
    public void handle(AuditEvent auditEvent) {
        if (auditEvent == null) {
            return;
        }

        // Process all handlers asynchronously to avoid blocking the main thread
        CompletableFuture.runAsync(() -> {
            for (AuditHandler handler : auditHandlers) {
                try {
                    if (handler.isEnabled()) {
                        handler.handle(auditEvent);
                    }
                } catch (Exception e) {
                    log.error("Error in audit handler {}: {}", handler.getClass().getSimpleName(), e.getMessage(), e);
                }
            }
        }, asyncExecutor);
    }

    /**
     * Handle audit event synchronously (for critical events)
     * Use sparingly as this can impact performance
     */
    public void handleSync(AuditEvent auditEvent) {
        if (auditEvent == null) {
            return;
        }

        for (AuditHandler handler : auditHandlers) {
            try {
                if (handler.isEnabled()) {
                    handler.handle(auditEvent);
                }
            } catch (Exception e) {
                log.error("Error in audit handler {}: {}", handler.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }

    /**
     * Check if any handlers are enabled
     */
    public boolean hasEnabledHandlers() {
        return auditHandlers.stream().anyMatch(AuditHandler::isEnabled);
    }

    /**
     * Get count of enabled handlers
     */
    public int getEnabledHandlerCount() {
        return (int) auditHandlers.stream().filter(AuditHandler::isEnabled).count();
    }

    /**
     * Shutdown all handlers
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down audit handler manager with {} handlers", auditHandlers.size());
        
        for (AuditHandler handler : auditHandlers) {
            try {
                handler.shutdown();
            } catch (Exception e) {
                log.error("Error shutting down handler {}: {}", handler.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
        
        log.info("Audit handler manager shutdown completed");
    }
}
