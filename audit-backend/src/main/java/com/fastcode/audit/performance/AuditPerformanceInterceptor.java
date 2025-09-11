package com.fastcode.audit.performance;

import com.fastcode.audit.domain.AuditEvent;
import com.fastcode.audit.handler.AuditHandlerManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;

/**
 * Performance-optimized audit interceptor
 * Ensures audit operations don't impact application performance
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditPerformanceInterceptor {

    private final AuditHandlerManager handlerManager;
    
    // Dedicated thread pool for audit operations
    private final Executor auditExecutor = createAuditExecutor();

    /**
     * Create optimized thread pool for audit operations
     */
    private Executor createAuditExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2, // Core pool size
            5, // Maximum pool size
            60L, TimeUnit.SECONDS, // Keep alive time
            new LinkedBlockingQueue<>(1000), // Queue size
            new ThreadFactory() {
                private int counter = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "audit-perf-" + (++counter));
                    t.setDaemon(true);
                    t.setPriority(Thread.MIN_PRIORITY); // Low priority to not impact main app
                    return t;
                }
            },
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    // If queue is full, log and continue (fail fast)
                    log.warn("Audit queue full, dropping audit event to maintain performance");
                }
            }
        );
        
        // Allow core threads to timeout
        executor.allowCoreThreadTimeOut(true);
        
        return executor;
    }

    /**
     * Process audit event asynchronously with performance safeguards
     */
    public void processAuditEvent(AuditEvent auditEvent) {
        if (auditEvent == null) {
            return;
        }

        // Process asynchronously with low priority
        CompletableFuture.runAsync(() -> {
            try {
                handlerManager.handle(auditEvent);
            } catch (Exception e) {
                log.error("Error processing audit event: {}", e.getMessage(), e);
            }
        }, auditExecutor);
    }

    /**
     * Process audit event synchronously (use only for critical events)
     */
    public void processAuditEventSync(AuditEvent auditEvent) {
        if (auditEvent == null) {
            return;
        }

        try {
            handlerManager.handleSync(auditEvent);
        } catch (Exception e) {
            log.error("Error processing audit event synchronously: {}", e.getMessage(), e);
        }
    }

    /**
     * Check if audit processing is healthy
     */
    public boolean isHealthy() {
        if (auditExecutor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) auditExecutor;
            return tpe.getActiveCount() < tpe.getMaximumPoolSize() && 
                   tpe.getQueue().remainingCapacity() > 0;
        }
        return true;
    }

    /**
     * Get audit processing statistics
     */
    public AuditPerformanceStats getStats() {
        if (auditExecutor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) auditExecutor;
            return AuditPerformanceStats.builder()
                .activeThreads(tpe.getActiveCount())
                .maxThreads(tpe.getMaximumPoolSize())
                .queueSize(tpe.getQueue().size())
                .queueCapacity(tpe.getQueue().remainingCapacity())
                .completedTasks(tpe.getCompletedTaskCount())
                .build();
        }
        return AuditPerformanceStats.builder().build();
    }

    /**
     * Shutdown the audit interceptor
     */
    public void shutdown() {
        if (auditExecutor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) auditExecutor;
            tpe.shutdown();
            try {
                if (!tpe.awaitTermination(5, TimeUnit.SECONDS)) {
                    tpe.shutdownNow();
                }
            } catch (InterruptedException e) {
                tpe.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        log.info("Audit performance interceptor shutdown completed");
    }
}
