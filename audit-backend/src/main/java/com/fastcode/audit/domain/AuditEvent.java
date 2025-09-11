package com.fastcode.audit.domain;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "audit")
public class AuditEvent {

    @Id
    @Column(name = "identifier", length = 200)
    private String identifier; // Unique identifier for the audit record

    @Column(name = "timestamp", nullable = false)
    private Timestamp timestamp; // The timestamp of the event

    @Column(name = "actor", nullable = false, length = 200)
    private String actor; // The actor who triggered the event

    @Column(name = "origin", length = 200)
    private String origin; // The origin of the event

    @Column(name = "action", nullable = false, length = 200)
    private String action; // The action performed

    @Column(name = "http_method", length = 50)
    private String httpMethod; // HTTP method for API calls

    @Column(name = "path", length = 500)
    private String path; // API path or entity path

    @Column(name = "entity_name", length = 200)
    private String entityName; // Name of the entity being audited

    @Column(name = "operation", length = 100)
    private String operation; // CRUD operation (CREATE, READ, UPDATE, DELETE)

    @Column(name = "response_status", length = 10)
    private String responseStatus; // HTTP response status

    @Column(name = "exception_type", length = 200)
    private String exceptionType; // Type of exception if any

    @Column(name = "elements", columnDefinition = "TEXT")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> elements; // JSON formatted details of the changes including entity changes, API details, etc.

}
