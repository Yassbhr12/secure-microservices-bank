package com.securebank.audit.exception;

public class InvalidInternalCredentialsException
    extends RuntimeException {

    public InvalidInternalCredentialsException() {
        super("Invalid internal service credentials");
    }
}
