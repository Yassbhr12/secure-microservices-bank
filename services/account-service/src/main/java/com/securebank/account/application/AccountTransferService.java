package com.securebank.account.application;

import com.securebank.account.domain.model.Account;
import com.securebank.account.domain.model.ProcessedTransfer;
import com.securebank.account.exception.AccountNotFoundException;
import com.securebank.account.exception.AccountTransferException;
import com.securebank.account.repository.AccountRepository;
import com.securebank.account.repository.ProcessedTransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AccountTransferService {

    private final AccountRepository accountRepository;
    private final ProcessedTransferRepository
        processedTransferRepository;
    private final Clock clock;

    public AccountTransferService(
        AccountRepository accountRepository,
        ProcessedTransferRepository
            processedTransferRepository,
        Clock clock
    ) {
        this.accountRepository = accountRepository;
        this.processedTransferRepository =
            processedTransferRepository;
        this.clock = clock;
    }

    @Transactional
    public AccountTransferResult execute(
        AccountTransferCommand command
    ) {
        var existing =
            processedTransferRepository.findById(
                command.transferId()
            );

        if (existing.isPresent()) {
            return replay(existing.get(), command);
        }

        List<UUID> accountIds = List.of(
                command.sourceAccountId(),
                command.destinationAccountId()
            )
            .stream()
            .sorted()
            .toList();

        List<Account> lockedAccounts =
            accountRepository.findAllByIdForUpdate(
                accountIds
            );

        if (lockedAccounts.size() != 2) {
            throw new AccountNotFoundException();
        }

        /*
         * Une autre requête a pu terminer le même
         * virement pendant l’attente des verrous.
         */
        existing = processedTransferRepository.findById(
            command.transferId()
        );

        if (existing.isPresent()) {
            return replay(existing.get(), command);
        }

        Account source = findAccount(
            lockedAccounts,
            command.sourceAccountId()
        );

        Account destination = findAccount(
            lockedAccounts,
            command.destinationAccountId()
        );

        if (!source.getOwnerId().equals(command.ownerId())) {
            /*
             * Réponse volontairement générique pour ne pas
             * révéler l’existence du compte.
             */
            throw new AccountNotFoundException();
        }

        Instant now = clock.instant();

        source.debit(
            command.amount(),
            command.currency(),
            now
        );

        destination.credit(
            command.amount(),
            command.currency(),
            now
        );

        ProcessedTransfer processedTransfer =
            ProcessedTransfer.completed(
                command.transferId(),
                command.ownerId(),
                command.sourceAccountId(),
                command.destinationAccountId(),
                command.amount(),
                command.currency(),
                command.requestHash(),
                source.getBalance(),
                destination.getBalance(),
                now
            );

        processedTransferRepository.saveAndFlush(
            processedTransfer
        );

        return AccountTransferResult.from(
            processedTransfer,
            false
        );
    }

    private AccountTransferResult replay(
        ProcessedTransfer existing,
        AccountTransferCommand command
    ) {
        boolean sameRequest = existing.represents(
            command.ownerId(),
            command.sourceAccountId(),
            command.destinationAccountId(),
            command.amount(),
            command.currency(),
            command.requestHash()
        );

        if (!sameRequest) {
            throw new AccountTransferException(
                "TRANSFER_ID_CONFLICT",
                "Transfer identifier was already used for another request"
            );
        }

        return AccountTransferResult.from(
            existing,
            true
        );
    }

    private Account findAccount(
        List<Account> accounts,
        UUID accountId
    ) {
        return accounts.stream()
            .filter(account ->
                account.getId().equals(accountId)
            )
            .findFirst()
            .orElseThrow(AccountNotFoundException::new);
    }
}
