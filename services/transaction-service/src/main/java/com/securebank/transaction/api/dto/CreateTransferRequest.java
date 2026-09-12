package com.securebank.transaction.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTransferRequest(

    @NotNull(message = "Source account is required")
    UUID sourceAccountId,

    @NotNull(message = "Destination account is required")
    UUID destinationAccountId,

    @NotNull(message = "Amount is required")
    @DecimalMin(
        value = "0.01",
        message = "Amount must be at least 0.01"
    )
    @Digits(
        integer = 17,
        fraction = 2,
        message = "Amount must have at most 17 integer digits and 2 decimals"
    )
    BigDecimal amount
) {

    @AssertTrue(
        message = "Source and destination accounts must be different"
    )
    public boolean isAccountsDifferent() {
        return sourceAccountId == null
            || destinationAccountId == null
            || !sourceAccountId.equals(
            destinationAccountId
        );
    }

    @Override
    public String toString() {
        return "CreateTransferRequest[REDACTED]";
    }
}
