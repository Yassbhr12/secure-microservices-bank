package com.securebank.account.application;

import com.securebank.account.domain.model.ProcessedTransfer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountTransferResult(
    UUID transferId,
    boolean alreadyProcessed,
    BigDecimal sourceBalanceAfter,
    BigDecimal destinationBalanceAfter,
    Instant processedAt
) {

    public static AccountTransferResult from(
        ProcessedTransfer transfer,
        boolean alreadyProcessed
    ) {
        return new AccountTransferResult(
            transfer.getTransferId(),
            alreadyProcessed,
            transfer.getSourceBalanceAfter(),
            transfer.getDestinationBalanceAfter(),
            transfer.getProcessedAt()
        );
    }
}
