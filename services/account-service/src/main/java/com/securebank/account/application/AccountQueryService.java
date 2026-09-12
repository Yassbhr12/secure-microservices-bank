package com.securebank.account.application;

import com.securebank.account.api.dto.AccountResponse;
import com.securebank.account.domain.model.Account;
import com.securebank.account.exception.AccountNotFoundException;
import com.securebank.account.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AccountQueryService {

    private final AccountRepository accountRepository;

    public AccountQueryService(
        AccountRepository accountRepository
    ) {
        this.accountRepository = accountRepository;
    }

    public List<AccountResponse> findAllForOwner(
        UUID ownerId
    ) {
        Objects.requireNonNull(
            ownerId,
            "ownerId is required"
        );

        return accountRepository
            .findAllByOwnerIdOrderByCreatedAtDesc(
                ownerId
            )
            .stream()
            .map(AccountResponse::from)
            .toList();
    }

    public AccountResponse findOneForOwner(
        UUID accountId,
        UUID ownerId
    ) {
        Objects.requireNonNull(
            accountId,
            "accountId is required"
        );

        Objects.requireNonNull(
            ownerId,
            "ownerId is required"
        );

        Account account = accountRepository
            .findByIdAndOwnerId(accountId, ownerId)
            .orElseThrow(AccountNotFoundException::new);

        return AccountResponse.from(account);
    }
}
