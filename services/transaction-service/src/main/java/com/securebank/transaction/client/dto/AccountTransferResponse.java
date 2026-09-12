package com.securebank.transaction.client.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountTransferResponse(
    UUID transferId,
    boolean alreadyProcessed,
    BigDecimal sourceBalanceAfter,
    BigDecimal destinationBalanceAfter,
    Instant processedAt
) {
}
