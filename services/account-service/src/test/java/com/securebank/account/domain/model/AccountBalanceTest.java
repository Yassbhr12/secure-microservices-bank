package com.securebank.account.domain.model;

import com.securebank.account.exception.AccountTransferException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountBalanceTest {

    private static final Instant NOW =
        Instant.parse("2026-09-12T12:00:00Z");

    @Test
    void shouldCreditAndDebitAccount() {
        Account account = newAccount();

        account.credit(
            new BigDecimal("500.00"),
            "MAD",
            NOW.plusSeconds(1)
        );

        account.debit(
            new BigDecimal("125.00"),
            "MAD",
            NOW.plusSeconds(2)
        );

        assertEquals(
            new BigDecimal("375.00"),
            account.getBalance()
        );
    }

    @Test
    void shouldRejectDebitWhenBalanceIsInsufficient() {
        Account account = newAccount();

        AccountTransferException exception =
            assertThrows(
                AccountTransferException.class,
                () -> account.debit(
                    new BigDecimal("100.00"),
                    "MAD",
                    NOW.plusSeconds(1)
                )
            );

        assertEquals(
            "INSUFFICIENT_FUNDS",
            exception.getCode()
        );

        assertEquals(
            new BigDecimal("0.00"),
            account.getBalance()
        );
    }

    @Test
    void shouldRejectOperationOnBlockedAccount() {
        Account account = newAccount();
        account.block(NOW.plusSeconds(1));

        AccountTransferException exception =
            assertThrows(
                AccountTransferException.class,
                () -> account.credit(
                    new BigDecimal("100.00"),
                    "MAD",
                    NOW.plusSeconds(2)
                )
            );

        assertEquals(
            "ACCOUNT_BLOCKED",
            exception.getCode()
        );
    }

    private Account newAccount() {
        return Account.open(
            UUID.randomUUID(),
            "SB12345678901234567890123456",
            "MAD",
            NOW
        );
    }
}
