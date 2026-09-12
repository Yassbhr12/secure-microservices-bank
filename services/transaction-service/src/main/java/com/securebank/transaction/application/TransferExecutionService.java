package com.securebank.transaction.application;

import com.securebank.transaction.api.dto.CreateTransferRequest;
import com.securebank.transaction.api.dto.TransferResponse;
import com.securebank.transaction.client.AccountServiceClient;
import com.securebank.transaction.client.AuditServiceClient;
import com.securebank.transaction.client.dto.AccountTransferResponse;
import com.securebank.transaction.domain.model.Transfer;
import com.securebank.transaction.domain.model.TransferStatus;
import com.securebank.transaction.exception.AccountServiceUnavailableException;
import com.securebank.transaction.exception.AccountTransferRejectedException;
import com.securebank.transaction.exception.TransferRejectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TransferExecutionService {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(
            TransferExecutionService.class
        );

    private final TransferIdempotencyService
        idempotencyService;

    private final AccountServiceClient
        accountServiceClient;

    private final TransferStateService stateService;

    private final AuditServiceClient
        auditServiceClient;

    public TransferExecutionService(
        TransferIdempotencyService idempotencyService,
        AccountServiceClient accountServiceClient,
        TransferStateService stateService,
        AuditServiceClient auditServiceClient
    ) {
        this.idempotencyService = idempotencyService;
        this.accountServiceClient = accountServiceClient;
        this.stateService = stateService;
        this.auditServiceClient = auditServiceClient;
    }

    public TransferResponse execute(
        UUID ownerId,
        String idempotencyKey,
        CreateTransferRequest request,
        String accessToken
    ) {
        TransferReservation reservation =
            idempotencyService.reserve(
                ownerId,
                idempotencyKey,
                request
            );

        Transfer transfer = reservation.transfer();

        if (transfer.getStatus()
            == TransferStatus.COMPLETED) {

            return TransferResponse.from(transfer);
        }

        if (transfer.getStatus()
            == TransferStatus.FAILED) {

            throw new TransferRejectedException(
                transfer.getFailureCode()
            );
        }

        try {
            AccountTransferResponse accountResponse =
                accountServiceClient.executeTransfer(
                    transfer,
                    accessToken
                );

            validateAccountResponse(
                transfer,
                accountResponse
            );

            Transfer completed =
                stateService.markCompleted(
                    transfer.getId()
                );

            auditServiceClient
                .recordTransferCompleted(completed);

            LOGGER.info(
                "Transfer completed: transferId={}",
                transfer.getId()
            );

            return TransferResponse.from(completed);

        } catch (
            AccountTransferRejectedException exception
        ) {
            Transfer failed =
                stateService.markFailed(
                    transfer.getId(),
                    exception.getFailureCode()
                );

            auditServiceClient
                .recordTransferFailed(failed);

            LOGGER.warn(
                "Transfer rejected: transferId={}, code={}",
                transfer.getId(),
                exception.getFailureCode()
            );

            throw new TransferRejectedException(
                exception.getFailureCode()
            );

        } catch (
            AccountServiceUnavailableException exception
        ) {
            /*
             * Le virement reste PENDING.
             * Le client peut réessayer avec la même clé.
             */
            LOGGER.warn(
                "Account service unavailable: transferId={}",
                transfer.getId()
            );

            throw exception;
        }
    }

    private void validateAccountResponse(
        Transfer transfer,
        AccountTransferResponse response
    ) {
        if (response.transferId() == null
            || !response.transferId().equals(
            transfer.getId()
        )) {

            throw new AccountServiceUnavailableException(
                "Account service returned an invalid response"
            );
        }
    }
}
