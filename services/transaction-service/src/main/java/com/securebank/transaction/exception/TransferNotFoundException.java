package com.securebank.transaction.exception;

public class TransferNotFoundException
    extends RuntimeException {

    public TransferNotFoundException() {
        super("Transfer not found");
    }
}
