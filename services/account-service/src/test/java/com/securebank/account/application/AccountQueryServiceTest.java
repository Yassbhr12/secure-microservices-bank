package com.securebank.account.application;

import com.securebank.account.domain.model.Account;
import com.securebank.account.exception.AccountNotFoundException;
import com.securebank.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AccountQueryServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private AccountQueryService accountQueryService;

    @BeforeEach
    void setUp() {
        accountQueryService =
            new AccountQueryService(accountRepository);
    }

    @Test
    void shouldRejectAccountBelongingToAnotherOwner() {
        UUID accountId = UUID.randomUUID();
        UUID secondOwnerId = UUID.randomUUID();

        given(
            accountRepository.findByIdAndOwnerId(
                accountId,
                secondOwnerId
            )
        ).willReturn(Optional.empty());

        assertThrows(
            AccountNotFoundException.class,
            () -> accountQueryService.findOneForOwner(
                accountId,
                secondOwnerId
            )
        );

        verify(accountRepository)
            .findByIdAndOwnerId(
                accountId,
                secondOwnerId
            );

        verify(accountRepository, never())
            .findById(any(UUID.class));
    }

    @Test
    void shouldReturnAccountForItsOwner() {
        UUID ownerId = UUID.randomUUID();

        Account account = Account.open(
            ownerId,
            "SB12345678901234567890123456",
            "MAD",
            Instant.parse("2026-09-12T10:00:00Z")
        );

        given(
            accountRepository.findByIdAndOwnerId(
                account.getId(),
                ownerId
            )
        ).willReturn(Optional.of(account));

        accountQueryService.findOneForOwner(
            account.getId(),
            ownerId
        );

        verify(accountRepository)
            .findByIdAndOwnerId(
                account.getId(),
                ownerId
            );
    }
}
