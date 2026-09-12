package com.securebank.transaction.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransferTest {

    private static final UUID OWNER_ID =
        UUID.randomUUID();

    private static final UUID SOURCE_ID =
        UUID.randomUUID();

    private static final UUID DESTINATION_ID =
        UUID.randomUUID();

    private static final Instant NOW =
        Instant.parse("2026-09-12T12:00:00Z");

    @Test
    void shouldCreatePendingTransfer() {
        Transfer transfer = newTransfer();

        assertEquals(
            TransferStatus.PENDING,
            transfer.getStatus()
        );

        assertEquals(
            new BigDecimal("100.00"),
            transfer.getAmount()
        );

        assertNull(transfer.getCompletedAt());
        assertNull(transfer.getFailureCode());
    }

    @Test
    void shouldCompletePendingTransfer() {
        Transfer transfer = newTransfer();
        Instant completedAt = NOW.plusSeconds(10);

        transfer.complete(completedAt);

        assertEquals(
            TransferStatus.COMPLETED,
            transfer.getStatus()
        );
        assertEquals(
            completedAt,
            transfer.getCompletedAt()
        );
        assertNull(transfer.getFailureCode());
    }

    @Test
    void shouldFailPendingTransfer() {
        Transfer transfer = newTransfer();
        Instant failedAt = NOW.plusSeconds(10);

        transfer.fail(
            "INSUFFICIENT_FUNDS",
            failedAt
        );

        assertEquals(
            TransferStatus.FAILED,
            transfer.getStatus()
        );
        assertEquals(
            "INSUFFICIENT_FUNDS",
            transfer.getFailureCode()
        );
        assertEquals(
            failedAt,
            transfer.getCompletedAt()
        );
    }

    @Test
    void shouldRejectTransferToSameAccount() {
        assertThrows(
            IllegalArgumentException.class,
            () -> Transfer.start(
                OWNER_ID,
                SOURCE_ID,
                SOURCE_ID,
                new BigDecimal("100.00"),
                "MAD",
                "idempotency-key-001",
                "a".repeat(64),
                NOW
            )
        );
    }

    private Transfer newTransfer() {
        return Transfer.start(
            OWNER_ID,
            SOURCE_ID,
            DESTINATION_ID,
            new BigDecimal("100.00"),
            "MAD",
            "idempotency-key-001",
            "a".repeat(64),
            NOW
        );
    }
}
