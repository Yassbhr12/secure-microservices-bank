package com.securebank.audit.exception;

public class InvalidAuditSearchException
    extends RuntimeException {

    public InvalidAuditSearchException(
        String message
    ) {
        super(message);
    }
}
