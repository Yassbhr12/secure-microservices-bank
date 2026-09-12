package com.securebank.audit.repository;

import com.securebank.audit.domain.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface AuditEventRepository
    extends JpaRepository<AuditEvent, UUID>,
    JpaSpecificationExecutor<AuditEvent> {
}
