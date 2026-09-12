package com.securebank.transaction.exception;

public class AccountTransferRejectedException
    extends RuntimeException {

    private final int statusCode;
    private final String failureCode;

    public AccountTransferRejectedException(
        int statusCode,
        String failureCode,
        String message
    ) {
        super(message);
        this.statusCode = statusCode;
        this.failureCode = failureCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getFailureCode() {
        return failureCode;
    }
}
