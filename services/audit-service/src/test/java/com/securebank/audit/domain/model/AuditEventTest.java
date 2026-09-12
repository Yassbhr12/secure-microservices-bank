package com.securebank.audit.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuditEventTest {

    private static final Instant OCCURRED_AT =
        Instant.parse("2026-09-12T10:00:00Z");

    private static final Instant RECEIVED_AT =
        Instant.parse("2026-09-12T10:00:01Z");

    @Test
    void shouldCreateAndNormalizeAuditEvent() {
        AuditEvent event = AuditEvent.record(
            " Transaction-Service ",
            "actor-123",
            "client",
            "transfer_completed",
            "transfer",
            "transfer-456",
            AuditResult.SUCCESS,
            AuditSeverity.INFO,
            "127.0.0.1",
            "correlation-789",
            " Transfer completed ",
            OCCURRED_AT,
            RECEIVED_AT
        );

        assertAll(
            () -> assertNotNull(event.getId()),

            () -> assertEquals(
                "transaction-service",
                event.getSourceService()
            ),

            () -> assertEquals(
                "CLIENT",
                event.getActorRole()
            ),

            () -> assertEquals(
                "TRANSFER_COMPLETED",
                event.getAction()
            ),

            () -> assertEquals(
                "TRANSFER",
                event.getResourceType()
            ),

            () -> assertEquals(
                "Transfer completed",
                event.getDetails()
            ),

            () -> assertEquals(
                AuditResult.SUCCESS,
                event.getResult()
            ),

            () -> assertEquals(
                AuditSeverity.INFO,
                event.getSeverity()
            ),

            () -> assertEquals(
                OCCURRED_AT,
                event.getOccurredAt()
            ),

            () -> assertEquals(
                RECEIVED_AT,
                event.getReceivedAt()
            )
        );
    }

    @Test
    void shouldRejectInvalidActionFormat() {
        assertThrows(
            IllegalArgumentException.class,
            () -> AuditEvent.record(
                "transaction-service",
                "actor-123",
                "CLIENT",
                "TRANSFER-COMPLETED",
                "TRANSFER",
                "transfer-456",
                AuditResult.SUCCESS,
                AuditSeverity.INFO,
                null,
                null,
                null,
                OCCURRED_AT,
                RECEIVED_AT
            )
        );
    }
}
