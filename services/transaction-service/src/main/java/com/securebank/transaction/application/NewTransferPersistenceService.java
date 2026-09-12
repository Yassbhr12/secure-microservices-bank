package com.securebank.transaction.application;

import com.securebank.transaction.api.dto.CreateTransferRequest;

import com.securebank.transaction.domain.model.Transfer;
import com.securebank.transaction.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class NewTransferPersistenceService {

    private static final String DEFAULT_CURRENCY = "MAD";

    private final TransferRepository transferRepository;
    private final Clock clock;

    public NewTransferPersistenceService(
        TransferRepository transferRepository,
        Clock clock
    ) {
        this.transferRepository = transferRepository;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transfer create(
        UUID ownerId,
        String idempotencyKey,
        String requestHash,
        CreateTransferRequest request
    ) {
        Transfer transfer = Transfer.start(
            ownerId,
            request.sourceAccountId(),
            request.destinationAccountId(),
            request.amount(),
            DEFAULT_CURRENCY,
            idempotencyKey,
            requestHash,
            clock.instant()
        );

        return transferRepository.saveAndFlush(transfer);
    }
}
