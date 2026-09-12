package com.securebank.account.application;

import com.securebank.account.api.dto.AccountResponse;
import com.securebank.account.domain.model.Account;
import com.securebank.account.exception.AccountNotFoundException;
import com.securebank.account.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

@Service
public class AccountAdministrationService {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(
            AccountAdministrationService.class
        );

    private final AccountRepository accountRepository;
    private final Clock clock;

    public AccountAdministrationService(
        AccountRepository accountRepository,
        Clock clock
    ) {
        this.accountRepository = accountRepository;
        this.clock = clock;
    }

    @Transactional
    public AccountResponse block(UUID accountId) {
        Account account = findAccount(accountId);

        account.block(clock.instant());
        accountRepository.flush();

        LOGGER.info(
            "Account block operation completed: accountId={}",
            accountId
        );

        return AccountResponse.from(account);
    }

    @Transactional
    public AccountResponse unblock(UUID accountId) {
        Account account = findAccount(accountId);

        account.unblock(clock.instant());
        accountRepository.flush();

        LOGGER.info(
            "Account unblock operation completed: accountId={}",
            accountId
        );

        return AccountResponse.from(account);
    }

    private Account findAccount(UUID accountId) {
        Objects.requireNonNull(
            accountId,
            "accountId is required"
        );

        return accountRepository
            .findById(accountId)
            .orElseThrow(AccountNotFoundException::new);
    }
}
