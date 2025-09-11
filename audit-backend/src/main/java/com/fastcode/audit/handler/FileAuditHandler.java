package com.fastcode.audit.handler;

import com.fastcode.audit.AuditPropertiesConfiguration;
import com.fastcode.audit.domain.AuditEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

/**
 * File audit handler for logging audit events to files with rotation
 * Similar to audit4j file handler but optimized for performance
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FileAuditHandler implements AuditHandler {

    private final AuditPropertiesConfiguration auditConfig;
    private final Executor asyncExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "audit-file-handler");
        t.setDaemon(true);
        return t;
    });

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private final ReentrantLock fileLock = new ReentrantLock();
    private volatile String currentFileName;
    private volatile BufferedWriter currentWriter;

    @Override
    public void handle(AuditEvent auditEvent) {
        if (!auditConfig.isAuditFileEnabled()) {
            return;
        }

        // Process asynchronously to avoid blocking the main thread
        CompletableFuture.runAsync(() -> {
            try {
                writeToFile(auditEvent);
            } catch (Exception e) {
                log.error("Error in file audit handler: {}", e.getMessage(), e);
            }
        }, asyncExecutor);
    }

    @Override
    public boolean isEnabled() {
        return auditConfig.isAuditFileEnabled();
    }

    /**
     * Write audit event to file
     */
    private void writeToFile(AuditEvent auditEvent) {
        fileLock.lock();
        try {
            String fileName = getCurrentFileName();
            BufferedWriter writer = getCurrentWriter(fileName);
            
            if (writer != null) {
                String formattedMessage = formatAuditEvent(auditEvent);
                writer.write(formattedMessage);
                writer.newLine();
                writer.flush();
            }
        } catch (IOException e) {
            log.error("Error writing to audit file: {}", e.getMessage(), e);
        } finally {
            fileLock.unlock();
        }
    }

    /**
     * Get current file name based on date
     */
    private String getCurrentFileName() {
        String today = LocalDate.now().format(FILE_DATE_FORMATTER);
        String fileName = auditConfig.getAuditFilePrefix() + today + ".log";
        return fileName;
    }

    /**
     * Get current writer, creating new one if needed
     */
    private BufferedWriter getCurrentWriter(String fileName) {
        if (!fileName.equals(currentFileName)) {
            closeCurrentWriter();
            currentFileName = fileName;
            currentWriter = createNewWriter(fileName);
        }
        return currentWriter;
    }

    /**
     * Create new file writer
     */
    private BufferedWriter createNewWriter(String fileName) {
        try {
            String filePath = auditConfig.getAuditFilePath();
            Path directory = Paths.get(filePath);
            
            // Create directory if it doesn't exist
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
            
            File file = new File(directory.toFile(), fileName);
            FileWriter fileWriter = new FileWriter(file, true); // Append mode
            return new BufferedWriter(fileWriter);
        } catch (IOException e) {
            log.error("Failed to create audit file writer: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Close current writer
     */
    private void closeCurrentWriter() {
        if (currentWriter != null) {
            try {
                currentWriter.close();
            } catch (IOException e) {
                log.error("Error closing audit file writer: {}", e.getMessage(), e);
            }
            currentWriter = null;
        }
    }

    /**
     * Format audit event for file output
     */
    private String formatAuditEvent(AuditEvent auditEvent) {
        StringBuilder sb = new StringBuilder();
        
        // Use layout template if configured, otherwise use default format
        String template = auditConfig.getAuditLayoutTemplate();
        if (template != null && !template.trim().isEmpty()) {
            return formatWithTemplate(auditEvent, template);
        }

        // Default format
        sb.append(auditEvent.getTimestamp().toLocalDateTime().format(DATE_FORMATTER));
        sb.append("|").append(auditEvent.getIdentifier());
        sb.append("|actor=").append(auditEvent.getActor());
        sb.append("|").append(auditEvent.getAction());
        sb.append("|origin=").append(auditEvent.getOrigin());

        // Add HTTP method and path if available
        if (auditEvent.getHttpMethod() != null) {
            sb.append("|method=").append(auditEvent.getHttpMethod());
        }
        if (auditEvent.getPath() != null) {
            sb.append("|path=").append(auditEvent.getPath());
        }

        // Add entity information if available
        if (auditEvent.getEntityName() != null) {
            sb.append("|entity=").append(auditEvent.getEntityName());
        }
        if (auditEvent.getOperation() != null) {
            sb.append("|operation=").append(auditEvent.getOperation());
        }

        // Add response status if available
        if (auditEvent.getResponseStatus() != null) {
            sb.append("|status=").append(auditEvent.getResponseStatus());
        }

        // Add exception type if available
        if (auditEvent.getExceptionType() != null) {
            sb.append("|exception=").append(auditEvent.getExceptionType());
        }

        // Add elements if available
        if (auditEvent.getElements() != null && !auditEvent.getElements().isEmpty()) {
            sb.append(" => ");
            formatElementsForFile(sb, auditEvent.getElements());
        }

        return sb.toString();
    }

    /**
     * Format with custom template
     */
    private String formatWithTemplate(AuditEvent auditEvent, String template) {
        String result = template;
        
        // Replace basic placeholders
        result = result.replace("${eventDate}", auditEvent.getTimestamp().toLocalDateTime().format(DATE_FORMATTER));
        result = result.replace("${uuid}", auditEvent.getIdentifier());
        result = result.replace("${actor}", auditEvent.getActor());
        result = result.replace("${action}", auditEvent.getAction());
        result = result.replace("${origin}", auditEvent.getOrigin());
        
        // Replace HTTP method and path
        result = result.replace("${httpMethod}", auditEvent.getHttpMethod() != null ? auditEvent.getHttpMethod() : "");
        result = result.replace("${path}", auditEvent.getPath() != null ? auditEvent.getPath() : "");
        
        // Replace entity information
        result = result.replace("${entityName}", auditEvent.getEntityName() != null ? auditEvent.getEntityName() : "");
        result = result.replace("${operation}", auditEvent.getOperation() != null ? auditEvent.getOperation() : "");
        
        // Replace response status
        result = result.replace("${responseStatus}", auditEvent.getResponseStatus() != null ? auditEvent.getResponseStatus() : "");
        
        // Replace exception type
        result = result.replace("${exceptionType}", auditEvent.getExceptionType() != null ? auditEvent.getExceptionType() : "");
        
        // Handle fields iteration (simplified implementation)
        if (result.contains("${foreach fields field}") && auditEvent.getElements() != null) {
            StringBuilder fieldsBuilder = new StringBuilder();
            for (Map.Entry<String, Object> entry : auditEvent.getElements().entrySet()) {
                if (fieldsBuilder.length() > 0) {
                    fieldsBuilder.append(", ");
                }
                Object value = entry.getValue();
                String typeName = (value != null) ? value.getClass().getSimpleName() : "null";
                String valueStr = (value != null) ? value.toString() : "null";
                fieldsBuilder.append(entry.getKey()).append(" ").append(typeName)
                           .append(":").append(valueStr);
            }
            result = result.replace("${foreach fields field}${field.name} ${field.type}:${field.value}, ${end}", fieldsBuilder.toString());
        }
        
        return result;
    }

    /**
     * Format elements map for file output
     */
    private void formatElementsForFile(StringBuilder sb, Map<String, Object> elements) {
        boolean first = true;
        for (Map.Entry<String, Object> entry : elements.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            first = false;

            Object value = entry.getValue();
            String typeName = (value != null) ? value.getClass().getSimpleName() : "null";
            String valueStr = (value != null) ? value.toString() : "null";
            sb.append(entry.getKey()).append(" ").append(typeName)
              .append(":").append(valueStr);
        }
    }

    @Override
    public void shutdown() {
        fileLock.lock();
        try {
            closeCurrentWriter();
        } finally {
            fileLock.unlock();
        }
        log.debug("File audit handler shutdown");
    }
}
