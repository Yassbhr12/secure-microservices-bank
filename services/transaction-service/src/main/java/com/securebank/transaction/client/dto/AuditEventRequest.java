package com.securebank.transaction.client.dto;

import com.securebank.transaction.domain.model.Transfer;

import java.time.Instant;
import java.util.Objects;

public record AuditEventRequest(
    String sourceService,
    String actorId,
    String actorRole,
    String action,
    String resourceType,
    String resourceId,
    String result,
    String severity,
    String ipAddress,
    String correlationId,
    String details,
    Instant occurredAt
) {

    public static AuditEventRequest transferCompleted(
        Transfer transfer
    ) {
        Objects.requireNonNull(
            transfer,
            "transfer is required"
        );

        return new AuditEventRequest(
            "transaction-service",
            transfer.getOwnerId().toString(),
            "CLIENT",
            "TRANSFER_COMPLETED",
            "TRANSFER",
            transfer.getId().toString(),
            "SUCCESS",
            "INFO",
            null,
            null,
            "Transfer completed successfully",
            requireCompletionTime(transfer)
        );
    }

    public static AuditEventRequest transferFailed(
        Transfer transfer
    ) {
        Objects.requireNonNull(
            transfer,
            "transfer is required"
        );

        String failureCode =
            transfer.getFailureCode() == null
                ? "UNKNOWN"
                : transfer.getFailureCode();

        return new AuditEventRequest(
            "transaction-service",
            transfer.getOwnerId().toString(),
            "CLIENT",
            "TRANSFER_FAILED",
            "TRANSFER",
            transfer.getId().toString(),
            "FAILURE",
            "WARNING",
            null,
            null,
            "Transfer rejected with code: "
                + failureCode,
            requireCompletionTime(transfer)
        );
    }

    private static Instant requireCompletionTime(
        Transfer transfer
    ) {
        return Objects.requireNonNull(
            transfer.getCompletedAt(),
            "Transfer completion time is required"
        );
    }
}
