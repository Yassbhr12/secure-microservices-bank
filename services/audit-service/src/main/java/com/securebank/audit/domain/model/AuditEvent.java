package com.securebank.audit.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Entity
@Immutable
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    @Column(
        name = "id",
        nullable = false,
        updatable = false
    )
    private UUID id;

    @Column(
        name = "occurred_at",
        nullable = false,
        updatable = false
    )
    private Instant occurredAt;

    @Column(
        name = "received_at",
        nullable = false,
        updatable = false
    )
    private Instant receivedAt;

    @Column(
        name = "source_service",
        nullable = false,
        length = 64,
        updatable = false
    )
    private String sourceService;

    @Column(
        name = "actor_id",
        length = 128,
        updatable = false
    )
    private String actorId;

    @Column(
        name = "actor_role",
        length = 32,
        updatable = false
    )
    private String actorRole;

    @Column(
        name = "action",
        nullable = false,
        length = 100,
        updatable = false
    )
    private String action;

    @Column(
        name = "resource_type",
        nullable = false,
        length = 64,
        updatable = false
    )
    private String resourceType;

    @Column(
        name = "resource_id",
        length = 128,
        updatable = false
    )
    private String resourceId;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "result",
        nullable = false,
        length = 16,
        updatable = false
    )
    private AuditResult result;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "severity",
        nullable = false,
        length = 16,
        updatable = false
    )
    private AuditSeverity severity;

    @Column(
        name = "ip_address",
        length = 45,
        updatable = false
    )
    private String ipAddress;

    @Column(
        name = "correlation_id",
        length = 100,
        updatable = false
    )
    private String correlationId;

    @Column(
        name = "details",
        columnDefinition = "TEXT",
        updatable = false
    )
    private String details;

    protected AuditEvent() {
        // Constructeur exigé par JPA.
    }

    private AuditEvent(
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
        String details,
        Instant occurredAt,
        Instant receivedAt
    ) {
        this.id = UUID.randomUUID();

        this.sourceService = normalizeSourceService(
            sourceService
        );

        this.actorId = optionalText(
            actorId,
            "actorId",
            128
        );

        this.actorRole = optionalCode(
            actorRole,
            "actorRole",
            32
        );

        this.action = requiredCode(
            action,
            "action",
            100
        );

        this.resourceType = requiredCode(
            resourceType,
            "resourceType",
            64
        );

        this.resourceId = optionalText(
            resourceId,
            "resourceId",
            128
        );

        this.result = Objects.requireNonNull(
            result,
            "result is required"
        );

        this.severity = Objects.requireNonNull(
            severity,
            "severity is required"
        );

        this.ipAddress = optionalText(
            ipAddress,
            "ipAddress",
            45
        );

        this.correlationId = optionalText(
            correlationId,
            "correlationId",
            100
        );

        this.details = optionalText(
            details,
            "details",
            4000
        );

        this.occurredAt = Objects.requireNonNull(
            occurredAt,
            "occurredAt is required"
        );

        this.receivedAt = Objects.requireNonNull(
            receivedAt,
            "receivedAt is required"
        );
    }

    public static AuditEvent record(
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
        String details,
        Instant occurredAt,
        Instant receivedAt
    ) {
        return new AuditEvent(
            sourceService,
            actorId,
            actorRole,
            action,
            resourceType,
            resourceId,
            result,
            severity,
            ipAddress,
            correlationId,
            details,
            occurredAt,
            receivedAt
        );
    }

    private static String normalizeSourceService(
        String sourceService
    ) {
        return requiredText(
            sourceService,
            "sourceService",
            64
        ).toLowerCase(Locale.ROOT);
    }

    private static String requiredCode(
        String value,
        String fieldName,
        int maximumLength
    ) {
        String normalized = requiredText(
            value,
            fieldName,
            maximumLength
        ).toUpperCase(Locale.ROOT);

        if (!normalized.matches("[A-Z0-9_]+")) {
            throw new IllegalArgumentException(
                fieldName
                    + " must contain only letters, numbers and underscores"
            );
        }

        return normalized;
    }

    private static String optionalCode(
        String value,
        String fieldName,
        int maximumLength
    ) {
        String normalized = optionalText(
            value,
            fieldName,
            maximumLength
        );

        if (normalized == null) {
            return null;
        }

        normalized = normalized.toUpperCase(Locale.ROOT);

        if (!normalized.matches("[A-Z0-9_]+")) {
            throw new IllegalArgumentException(
                fieldName
                    + " must contain only letters, numbers and underscores"
            );
        }

        return normalized;
    }

    private static String requiredText(
        String value,
        String fieldName,
        int maximumLength
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                fieldName + " is required"
            );
        }

        String normalized = value.trim();

        if (normalized.length() > maximumLength) {
            throw new IllegalArgumentException(
                fieldName
                    + " must not exceed "
                    + maximumLength
                    + " characters"
            );
        }

        return normalized;
    }

    private static String optionalText(
        String value,
        String fieldName,
        int maximumLength
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        if (normalized.length() > maximumLength) {
            throw new IllegalArgumentException(
                fieldName
                    + " must not exceed "
                    + maximumLength
                    + " characters"
            );
        }

        return normalized;
    }

    public UUID getId() {
        return id;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public String getSourceService() {
        return sourceService;
    }

    public String getActorId() {
        return actorId;
    }

    public String getActorRole() {
        return actorRole;
    }

    public String getAction() {
        return action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public AuditResult getResult() {
        return result;
    }

    public AuditSeverity getSeverity() {
        return severity;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null
            || Hibernate.getClass(this)
            != Hibernate.getClass(other)) {

            return false;
        }

        AuditEvent otherEvent = (AuditEvent) other;

        return id != null && id.equals(otherEvent.id);
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
