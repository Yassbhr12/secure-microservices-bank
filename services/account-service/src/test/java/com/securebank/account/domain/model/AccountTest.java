package com.securebank.account.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountTest {

    @Test
    void shouldOpenAnActiveAccountWithZeroBalance() {
        UUID ownerId = UUID.randomUUID();
        Instant now =
            Instant.parse("2026-09-12T10:00:00Z");

        Account account = Account.open(
            ownerId,
            "SB12345678901234567890123456",
            "mad",
            now
        );

        assertEquals(ownerId, account.getOwnerId());
        assertEquals(
            new BigDecimal("0.00"),
            account.getBalance()
        );
        assertEquals("MAD", account.getCurrency());
        assertEquals(
            AccountStatus.ACTIVE,
            account.getStatus()
        );
        assertFalse(account.isBlocked());
        assertEquals(now, account.getCreatedAt());
        assertEquals(now, account.getUpdatedAt());
    }

    @Test
    void shouldBlockAndUnblockAccountIdempotently() {
        Instant createdAt =
            Instant.parse("2026-09-12T10:00:00Z");

        Account account = Account.open(
            UUID.randomUUID(),
            "SB12345678901234567890123456",
            "MAD",
            createdAt
        );

        Instant blockedAt = createdAt.plusSeconds(60);
        account.block(blockedAt);

        assertTrue(account.isBlocked());
        assertEquals(
            AccountStatus.BLOCKED,
            account.getStatus()
        );
        assertEquals(blockedAt, account.getUpdatedAt());

        account.block(blockedAt.plusSeconds(60));

        assertEquals(blockedAt, account.getUpdatedAt());

        Instant unblockedAt =
            blockedAt.plusSeconds(120);

        account.unblock(unblockedAt);

        assertFalse(account.isBlocked());
        assertEquals(
            AccountStatus.ACTIVE,
            account.getStatus()
        );
        assertEquals(
            unblockedAt,
            account.getUpdatedAt()
        );
    }
}
