package com.securebank.transaction.application;

import com.securebank.transaction.domain.model.Transfer;
import com.securebank.transaction.exception.TransferNotFoundException;
import com.securebank.transaction.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class TransferStateService {

    private final TransferRepository transferRepository;
    private final Clock clock;

    public TransferStateService(
        TransferRepository transferRepository,
        Clock clock
    ) {
        this.transferRepository = transferRepository;
        this.clock = clock;
    }

    @Transactional
    public Transfer markCompleted(UUID transferId) {
        Transfer transfer = findForUpdate(transferId);

        transfer.complete(clock.instant());
        transferRepository.flush();

        return transfer;
    }

    @Transactional
    public Transfer markFailed(
        UUID transferId,
        String failureCode
    ) {
        Transfer transfer = findForUpdate(transferId);

        transfer.fail(
            failureCode,
            clock.instant()
        );

        transferRepository.flush();

        return transfer;
    }

    private Transfer findForUpdate(UUID transferId) {
        return transferRepository
            .findByIdForUpdate(transferId)
            .orElseThrow(TransferNotFoundException::new);
    }
}
