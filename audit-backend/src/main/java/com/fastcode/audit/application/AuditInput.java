package com.fastcode.audit.application;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Setter
@Getter
public class AuditInput {
    private String userId;
    private String sessionId;
    private String action;
    private Map<String, Object> details;
}
