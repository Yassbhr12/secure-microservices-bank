package com.securebank.account.application;

import com.securebank.account.exception.AccountTransferException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public record AccountTransferCommand(
    UUID transferId,
    UUID ownerId,
    UUID sourceAccountId,
    UUID destinationAccountId,
    BigDecimal amount,
    String currency,
    String requestHash
) {

    public AccountTransferCommand {
        Objects.requireNonNull(
            transferId,
            "transferId is required"
        );
        Objects.requireNonNull(
            ownerId,
            "ownerId is required"
        );
        Objects.requireNonNull(
            sourceAccountId,
            "sourceAccountId is required"
        );
        Objects.requireNonNull(
            destinationAccountId,
            "destinationAccountId is required"
        );

        if (sourceAccountId.equals(destinationAccountId)) {
            throw invalid(
                "Source and destination accounts must be different"
            );
        }

        if (amount == null) {
            throw invalid("Transfer amount is required");
        }

        try {
            amount = amount.setScale(
                2,
                RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException exception) {
            throw invalid(
                "Transfer amount must have at most 2 decimal places"
            );
        }

        if (amount.signum() <= 0 || amount.precision() > 19) {
            throw invalid("Transfer amount is invalid");
        }

        if (currency == null) {
            throw invalid("Currency is required");
        }

        currency = currency
            .trim()
            .toUpperCase(Locale.ROOT);

        if (!currency.matches("[A-Z]{3}")) {
            throw invalid("Currency must contain 3 letters");
        }

        if (requestHash == null) {
            throw invalid("Request hash is required");
        }

        requestHash = requestHash.trim();

        if (!requestHash.matches("[0-9a-f]{64}")) {
            throw invalid(
                "Request hash must be a lowercase SHA-256 value"
            );
        }
    }

    private static AccountTransferException invalid(
        String message
    ) {
        return new AccountTransferException(
            "INVALID_TRANSFER_REQUEST",
            message
        );
    }
}
