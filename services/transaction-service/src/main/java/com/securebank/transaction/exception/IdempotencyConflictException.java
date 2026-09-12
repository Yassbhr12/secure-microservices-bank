package com.securebank.transaction.exception;

public class IdempotencyConflictException
    extends RuntimeException {

    public IdempotencyConflictException() {
        super(
            "This idempotency key was already used for a different transfer request."
        );
    }
}
