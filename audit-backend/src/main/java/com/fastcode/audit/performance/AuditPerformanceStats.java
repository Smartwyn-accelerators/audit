package com.fastcode.audit.performance;

import lombok.Builder;
import lombok.Data;

/**
 * Performance statistics for audit operations
 */
@Data
@Builder
public class AuditPerformanceStats {
    
    private int activeThreads;
    private int maxThreads;
    private int queueSize;
    private int queueCapacity;
    private long completedTasks;
    
    /**
     * Get queue utilization percentage
     */
    public double getQueueUtilization() {
        if (queueCapacity == 0) {
            return 0.0;
        }
        return (double) queueSize / (queueSize + queueCapacity) * 100.0;
    }
    
    /**
     * Get thread utilization percentage
     */
    public double getThreadUtilization() {
        if (maxThreads == 0) {
            return 0.0;
        }
        return (double) activeThreads / maxThreads * 100.0;
    }
    
    /**
     * Check if system is under stress
     */
    public boolean isUnderStress() {
        return getQueueUtilization() > 80.0 || getThreadUtilization() > 80.0;
    }
}
