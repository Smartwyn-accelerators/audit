package com.fastcode.audit.application;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
public class AuditEventDto {

    private String identifier;
    private Timestamp timestamp;
    private String actor;
    private String origin;
    private String action;
    private List<AuditField> elements;

}
