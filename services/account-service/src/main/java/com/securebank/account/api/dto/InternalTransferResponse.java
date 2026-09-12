package com.securebank.account.api.dto;

import com.securebank.account.application.AccountTransferResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InternalTransferResponse(
    UUID transferId,
    boolean alreadyProcessed,
    BigDecimal sourceBalanceAfter,
    BigDecimal destinationBalanceAfter,
    Instant processedAt
) {

    public static InternalTransferResponse from(
        AccountTransferResult result
    ) {
        return new InternalTransferResponse(
            result.transferId(),
            result.alreadyProcessed(),
            result.sourceBalanceAfter(),
            result.destinationBalanceAfter(),
            result.processedAt()
        );
    }
}
