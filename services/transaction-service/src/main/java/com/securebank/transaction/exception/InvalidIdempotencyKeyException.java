package com.securebank.transaction.exception;

public class InvalidIdempotencyKeyException
    extends RuntimeException {

    public InvalidIdempotencyKeyException() {
        super(
            "The Idempotency-Key header must contain between 8 and 128 characters."
        );
    }
}
