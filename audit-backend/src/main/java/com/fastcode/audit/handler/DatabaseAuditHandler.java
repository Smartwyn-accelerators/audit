package com.fastcode.audit.handler;

import com.fastcode.audit.AuditPropertiesConfiguration;
import com.fastcode.audit.domain.AuditEvent;
import com.fastcode.audit.domain.IAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Database audit handler for persisting audit events to database
 * Similar to audit4j database handler but optimized for performance
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseAuditHandler implements AuditHandler {

    @Qualifier("auditRepository")
    private final IAuditRepository auditRepository;
    private final AuditPropertiesConfiguration auditConfig;
    private final Executor asyncExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "audit-database-handler");
        t.setDaemon(true);
        return t;
    });

    @Override
    public void handle(AuditEvent auditEvent) {
        if (!auditConfig.isAuditDatabaseEnabled()) {
            return;
        }

        // Process asynchronously to avoid blocking the main thread
        CompletableFuture.runAsync(() -> {
            try {
                auditRepository.save(auditEvent);
                log.debug("Database audit event saved: {} by {}", auditEvent.getAction(), auditEvent.getActor());
            } catch (Exception e) {
                log.error("Error saving audit event to database: {}", e.getMessage(), e);
            }
        }, asyncExecutor);
    }

    @Override
    public boolean isEnabled() {
        return auditConfig.isAuditDatabaseEnabled();
    }

    @Override
    public void shutdown() {
        // No specific shutdown needed for database handler
        log.debug("Database audit handler shutdown");
    }
}
