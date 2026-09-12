package com.securebank.account.api.dto;

import com.securebank.account.application.AccountTransferCommand;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.UUID;

public record InternalTransferRequest(

    @NotNull
    UUID transferId,

    @NotNull
    UUID sourceAccountId,

    @NotNull
    UUID destinationAccountId,

    @NotNull
    @DecimalMin(value = "0.01")
    @Digits(integer = 17, fraction = 2)
    BigDecimal amount,

    @NotNull
    @Pattern(regexp = "[A-Z]{3}")
    String currency,

    @NotNull
    @Pattern(regexp = "[0-9a-f]{64}")
    String requestHash
) {

    public AccountTransferCommand toCommand(
        UUID ownerId
    ) {
        return new AccountTransferCommand(
            transferId,
            ownerId,
            sourceAccountId,
            destinationAccountId,
            amount,
            currency,
            requestHash
        );
    }
}
