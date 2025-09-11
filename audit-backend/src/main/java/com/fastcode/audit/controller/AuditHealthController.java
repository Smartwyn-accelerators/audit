package com.fastcode.audit.controller;

import com.fastcode.audit.handler.AuditHandlerManager;
import com.fastcode.audit.performance.AuditPerformanceInterceptor;
import com.fastcode.audit.performance.AuditPerformanceStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for audit system
 * Provides monitoring endpoints for audit performance and health
 */
@RestController
@RequestMapping("/audit/health")
@RequiredArgsConstructor
@Slf4j
public class AuditHealthController {

    private final AuditHandlerManager handlerManager;
    private final AuditPerformanceInterceptor performanceInterceptor;

    /**
     * Get audit system health status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // Check if any handlers are enabled
            boolean hasEnabledHandlers = handlerManager.hasEnabledHandlers();
            int enabledHandlerCount = handlerManager.getEnabledHandlerCount();
            
            // Check performance health
            boolean isHealthy = performanceInterceptor.isHealthy();
            AuditPerformanceStats stats = performanceInterceptor.getStats();
            
            status.put("status", isHealthy ? "HEALTHY" : "DEGRADED");
            status.put("enabledHandlers", enabledHandlerCount);
            status.put("hasEnabledHandlers", hasEnabledHandlers);
            status.put("performance", stats);
            status.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error checking audit health: {}", e.getMessage(), e);
            status.put("status", "ERROR");
            status.put("error", e.getMessage());
            status.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(status);
        }
    }

    /**
     * Get detailed performance statistics
     */
    @GetMapping("/performance")
    public ResponseEntity<AuditPerformanceStats> getPerformanceStats() {
        try {
            AuditPerformanceStats stats = performanceInterceptor.getStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting performance stats: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get audit configuration summary
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfigSummary() {
        Map<String, Object> config = new HashMap<>();
        
        try {
            int enabledHandlerCount = handlerManager.getEnabledHandlerCount();
            boolean hasEnabledHandlers = handlerManager.hasEnabledHandlers();
            
            config.put("enabledHandlers", enabledHandlerCount);
            config.put("hasEnabledHandlers", hasEnabledHandlers);
            config.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("Error getting config summary: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
