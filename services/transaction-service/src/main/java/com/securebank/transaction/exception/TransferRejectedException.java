package com.securebank.transaction.exception;

public class TransferRejectedException
    extends RuntimeException {

    private final String failureCode;

    public TransferRejectedException(
        String failureCode
    ) {
        super("Transfer was rejected");
        this.failureCode =
            failureCode == null
                || failureCode.isBlank()
                ? "TRANSFER_REJECTED"
                : failureCode;
    }

    public String getFailureCode() {
        return failureCode;
    }
}
