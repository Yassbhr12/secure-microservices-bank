package com.securebank.transaction.application;



import com.securebank.transaction.domain.model.Transfer;

import java.util.Objects;

public record TransferReservation(
    Transfer transfer,
    boolean newlyCreated
) {

    public TransferReservation {
        Objects.requireNonNull(
            transfer,
            "transfer must not be null"
        );
    }

    public static TransferReservation created(
        Transfer transfer
    ) {
        return new TransferReservation(transfer, true);
    }

    public static TransferReservation existing(
        Transfer transfer
    ) {
        return new TransferReservation(transfer, false);
    }
}
