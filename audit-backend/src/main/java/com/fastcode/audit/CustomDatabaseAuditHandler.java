package com.fastcode.audit;

import org.audit4j.core.dto.AuditEvent;
import org.audit4j.core.dto.EventBatch;
import org.audit4j.core.exception.HandlerException;
import org.audit4j.handler.db.DatabaseAuditHandler;

public class CustomDatabaseAuditHandler extends DatabaseAuditHandler {

    @Override
    public void handle(AuditEvent auditEvent) throws HandlerException {

    }

    @Override
    public void handle(String s) throws HandlerException {

    }

    @Override
    public void handle(EventBatch eventBatch) throws HandlerException {

    }
}
