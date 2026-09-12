package com.securebank.transaction.client.dto;

import com.securebank.transaction.domain.model.Transfer;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountTransferRequest(
    UUID transferId,
    UUID sourceAccountId,
    UUID destinationAccountId,
    BigDecimal amount,
    String currency,
    String requestHash
) {

    public static AccountTransferRequest from(
        Transfer transfer
    ) {
        return new AccountTransferRequest(
            transfer.getId(),
            transfer.getSourceAccountId(),
            transfer.getDestinationAccountId(),
            transfer.getAmount(),
            transfer.getCurrency(),
            transfer.getRequestHash()
        );
    }
}
