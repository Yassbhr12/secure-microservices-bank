package com.securebank.account.application;

import com.securebank.account.api.dto.AccountResponse;
import com.securebank.account.domain.model.Account;
import com.securebank.account.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class AccountCreationService {

    private static final String DEFAULT_CURRENCY = "MAD";
    private static final int MAX_GENERATION_ATTEMPTS = 5;

    private final AccountRepository accountRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final Clock clock;

    public AccountCreationService(
        AccountRepository accountRepository,
        AccountNumberGenerator accountNumberGenerator,
        Clock clock
    ) {
        this.accountRepository = accountRepository;
        this.accountNumberGenerator =
            accountNumberGenerator;
        this.clock = clock;
    }

    @Transactional
    public AccountResponse createFor(UUID ownerId) {
        Objects.requireNonNull(
            ownerId,
            "ownerId is required"
        );

        String accountNumber =
            generateUniqueAccountNumber();

        Instant now = clock.instant();

        Account account = Account.open(
            ownerId,
            accountNumber,
            DEFAULT_CURRENCY,
            now
        );

        Account savedAccount =
            accountRepository.saveAndFlush(account);

        return AccountResponse.from(savedAccount);
    }

    private String generateUniqueAccountNumber() {
        for (int attempt = 0;
             attempt < MAX_GENERATION_ATTEMPTS;
             attempt++) {

            String candidate =
                accountNumberGenerator.generate();

            if (!accountRepository
                .existsByAccountNumber(candidate)) {

                return candidate;
            }
        }

        throw new IllegalStateException(
            "Unable to generate a unique account number"
        );
    }
}
