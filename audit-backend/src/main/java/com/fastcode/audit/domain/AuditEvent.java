package com.fastcode.audit.domain;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;

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

    @Column(name = "elements", length = 70000)
    private String elements; // Details of the changes in a serialized format

}
