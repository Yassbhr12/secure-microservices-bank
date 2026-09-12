package com.securebank.audit.dto;

import com.securebank.audit.domain.model.AuditResult;
import com.securebank.audit.domain.model.AuditSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateAuditEventRequest(

    @NotBlank
    @Size(max = 64)
    @Pattern(
        regexp = "^[A-Za-z0-9-]+$",
        message = "sourceService has an invalid format"
    )
    String sourceService,

    @Size(max = 128)
    String actorId,

    @Size(max = 32)
    @Pattern(
        regexp = "^[A-Za-z0-9_]+$",
        message = "actorRole has an invalid format"
    )
    String actorRole,

    @NotBlank
    @Size(max = 100)
    @Pattern(
        regexp = "^[A-Za-z0-9_]+$",
        message = "action has an invalid format"
    )
    String action,

    @NotBlank
    @Size(max = 64)
    @Pattern(
        regexp = "^[A-Za-z0-9_]+$",
        message = "resourceType has an invalid format"
    )
    String resourceType,

    @Size(max = 128)
    String resourceId,

    @NotNull
    AuditResult result,

    @NotNull
    AuditSeverity severity,

    @Size(max = 45)
    String ipAddress,

    @Size(max = 100)
    String correlationId,

    @Size(max = 4000)
    String details,

    @NotNull
    Instant occurredAt
) {
}
