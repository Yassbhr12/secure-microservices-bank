package com.securebank.transaction.api.dto;

import com.securebank.transaction.domain.model.Transfer;
import com.securebank.transaction.domain.model.TransferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
    UUID transferId,
    UUID sourceAccountId,
    UUID destinationAccountId,
    BigDecimal amount,
    String currency,
    TransferStatus status,
    String failureCode,
    Instant createdAt,
    Instant completedAt
) {

    public static TransferResponse from(
        Transfer transfer
    ) {
        return new TransferResponse(
            transfer.getId(),
            transfer.getSourceAccountId(),
            transfer.getDestinationAccountId(),
            transfer.getAmount(),
            transfer.getCurrency(),
            transfer.getStatus(),
            transfer.getFailureCode(),
            transfer.getCreatedAt(),
            transfer.getCompletedAt()
        );
    }
}
