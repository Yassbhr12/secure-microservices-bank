package com.securebank.account.api.dto;

import com.securebank.account.domain.model.Account;
import com.securebank.account.domain.model.AccountStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
    UUID accountId,
    String accountNumber,
    BigDecimal balance,
    String currency,
    AccountStatus status,
    Instant createdAt,
    Instant updatedAt
) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
            account.getId(),
            account.getAccountNumber(),
            account.getBalance(),
            account.getCurrency(),
            account.getStatus(),
            account.getCreatedAt(),
            account.getUpdatedAt()
        );
    }
}
