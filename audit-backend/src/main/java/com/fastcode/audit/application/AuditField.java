package com.fastcode.audit.application;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Custom Field class to replace audit4j Field
 * Represents a field in an audit event
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditField {
    private String name;
    private String value;
    private String type;
}
