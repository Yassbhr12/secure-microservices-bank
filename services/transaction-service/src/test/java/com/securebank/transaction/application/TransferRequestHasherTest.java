package com.securebank.transaction.application;

import com.securebank.transaction.api.dto.CreateTransferRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransferRequestHasherTest {

    private final TransferRequestHasher hasher =
        new TransferRequestHasher();

    @Test
    void shouldProduceSameHashForEquivalentAmounts() {
        UUID sourceId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();

        CreateTransferRequest first =
            new CreateTransferRequest(
                sourceId,
                destinationId,
                new BigDecimal("100")
            );

        CreateTransferRequest second =
            new CreateTransferRequest(
                sourceId,
                destinationId,
                new BigDecimal("100.00")
            );

        assertEquals(
            hasher.hash(first),
            hasher.hash(second)
        );
    }

    @Test
    void shouldProduceDifferentHashWhenAmountChanges() {
        UUID sourceId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();

        CreateTransferRequest first =
            new CreateTransferRequest(
                sourceId,
                destinationId,
                new BigDecimal("100.00")
            );

        CreateTransferRequest second =
            new CreateTransferRequest(
                sourceId,
                destinationId,
                new BigDecimal("200.00")
            );

        assertNotEquals(
            hasher.hash(first),
            hasher.hash(second)
        );
    }

    @Test
    void shouldProduceLowercaseSha256Hash() {
        CreateTransferRequest request =
            new CreateTransferRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00")
            );

        String hash = hasher.hash(request);

        assertTrue(
            hash.matches("[0-9a-f]{64}")
        );
    }
}
