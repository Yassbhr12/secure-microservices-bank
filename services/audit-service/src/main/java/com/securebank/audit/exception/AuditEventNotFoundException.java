package com.securebank.audit.exception;

import java.util.UUID;

public class AuditEventNotFoundException
    extends RuntimeException {

    public AuditEventNotFoundException(UUID eventId) {
        super("Audit event not found: " + eventId);
    }
}
