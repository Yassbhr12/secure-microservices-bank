package com.securebank.transaction.application;

import com.securebank.transaction.api.dto.TransferResponse;
import com.securebank.transaction.domain.model.Transfer;
import com.securebank.transaction.exception.TransferNotFoundException;
import com.securebank.transaction.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class TransferQueryService {

    private final TransferRepository transferRepository;

    public TransferQueryService(
        TransferRepository transferRepository
    ) {
        this.transferRepository = transferRepository;
    }

    @Transactional(readOnly = true)
    public List<TransferResponse> findAllForOwner(
        UUID ownerId
    ) {
        Objects.requireNonNull(
            ownerId,
            "ownerId is required"
        );

        return transferRepository
            .findAllByOwnerIdOrderByCreatedAtDesc(
                ownerId
            )
            .stream()
            .map(TransferResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public TransferResponse findForOwner(
        UUID transferId,
        UUID ownerId
    ) {
        Objects.requireNonNull(
            transferId,
            "transferId is required"
        );
        Objects.requireNonNull(
            ownerId,
            "ownerId is required"
        );

        Transfer transfer = transferRepository
            .findByIdAndOwnerId(
                transferId,
                ownerId
            )
            .orElseThrow(
                TransferNotFoundException::new
            );

        return TransferResponse.from(transfer);
    }
}
