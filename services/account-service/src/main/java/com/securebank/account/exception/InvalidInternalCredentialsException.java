package com.securebank.account.exception;

public class InvalidInternalCredentialsException
    extends RuntimeException {

    public InvalidInternalCredentialsException() {
        super("Invalid internal service credentials");
    }
}
