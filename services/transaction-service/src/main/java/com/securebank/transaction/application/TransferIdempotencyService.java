package com.securebank.transaction.application;

import com.securebank.transaction.api.dto.CreateTransferRequest;

import com.securebank.transaction.domain.model.Transfer;
import com.securebank.transaction.exception.IdempotencyConflictException;
import com.securebank.transaction.exception.InvalidIdempotencyKeyException;
import com.securebank.transaction.repository.TransferRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransferIdempotencyService {

    private static final int MINIMUM_KEY_LENGTH = 8;
    private static final int MAXIMUM_KEY_LENGTH = 128;

    private final TransferRepository transferRepository;
    private final TransferRequestHasher requestHasher;
    private final NewTransferPersistenceService persistenceService;

    public TransferIdempotencyService(
        TransferRepository transferRepository,
        TransferRequestHasher requestHasher,
        NewTransferPersistenceService persistenceService
    ) {
        this.transferRepository = transferRepository;
        this.requestHasher = requestHasher;
        this.persistenceService = persistenceService;
    }

    public TransferReservation reserve(
        UUID ownerId,
        String rawIdempotencyKey,
        CreateTransferRequest request
    ) {
        Objects.requireNonNull(
            ownerId,
            "ownerId must not be null"
        );
        Objects.requireNonNull(
            request,
            "request must not be null"
        );

        String idempotencyKey =
            normalizeKey(rawIdempotencyKey);

        String requestHash =
            requestHasher.hash(request);

        Optional<Transfer> existingTransfer =
            transferRepository
                .findByOwnerIdAndIdempotencyKey(
                    ownerId,
                    idempotencyKey
                );

        if (existingTransfer.isPresent()) {
            return validateExistingTransfer(
                existingTransfer.get(),
                requestHash
            );
        }

        try {
            Transfer createdTransfer =
                persistenceService.create(
                    ownerId,
                    idempotencyKey,
                    requestHash,
                    request
                );

            return TransferReservation.created(
                createdTransfer
            );
        } catch (
            DataIntegrityViolationException exception
        ) {
            /*
             * Une requête concurrente a éventuellement créé
             * la même clé juste avant celle-ci.
             */
            Transfer concurrentTransfer =
                transferRepository
                    .findByOwnerIdAndIdempotencyKey(
                        ownerId,
                        idempotencyKey
                    )
                    .orElseThrow(() -> exception);

            return validateExistingTransfer(
                concurrentTransfer,
                requestHash
            );
        }
    }

    private TransferReservation validateExistingTransfer(
        Transfer existingTransfer,
        String requestHash
    ) {
        if (!existingTransfer
            .getRequestHash()
            .equals(requestHash)) {

            throw new IdempotencyConflictException();
        }

        return TransferReservation.existing(
            existingTransfer
        );
    }

    private String normalizeKey(
        String rawIdempotencyKey
    ) {
        if (rawIdempotencyKey == null) {
            throw new InvalidIdempotencyKeyException();
        }

        String normalizedKey =
            rawIdempotencyKey.trim();

        if (normalizedKey.length()
            < MINIMUM_KEY_LENGTH
            || normalizedKey.length()
            > MAXIMUM_KEY_LENGTH) {

            throw new InvalidIdempotencyKeyException();
        }

        return normalizedKey;
    }
}
