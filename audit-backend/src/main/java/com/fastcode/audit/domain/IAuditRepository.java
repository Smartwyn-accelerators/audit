package com.fastcode.audit.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository("auditRepository")
public interface IAuditRepository extends JpaRepository<AuditEvent, String>, QuerydslPredicateExecutor<AuditEvent> {

    @Query("SELECT a.action from AuditEvent a GROUP BY a.action")
    List<String> findAllAction();
}