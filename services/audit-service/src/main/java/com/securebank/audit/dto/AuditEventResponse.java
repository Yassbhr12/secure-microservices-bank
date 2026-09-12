package com.securebank.audit.dto;

import com.securebank.audit.domain.model.AuditEvent;
import com.securebank.audit.domain.model.AuditResult;
import com.securebank.audit.domain.model.AuditSeverity;

import java.time.Instant;
import java.util.UUID;

public record AuditEventResponse(
    UUID id,
    Instant occurredAt,
    Instant receivedAt,
    String sourceService,
    String actorId,
    String actorRole,
    String action,
    String resourceType,
    String resourceId,
    AuditResult result,
    AuditSeverity severity,
    String ipAddress,
    String correlationId,
    String details
) {

    public static AuditEventResponse from(
        AuditEvent event
    ) {
        return new AuditEventResponse(
            event.getId(),
            event.getOccurredAt(),
            event.getReceivedAt(),
            event.getSourceService(),
            event.getActorId(),
            event.getActorRole(),
            event.getAction(),
            event.getResourceType(),
            event.getResourceId(),
            event.getResult(),
            event.getSeverity(),
            event.getIpAddress(),
            event.getCorrelationId(),
            event.getDetails()
        );
    }
}
