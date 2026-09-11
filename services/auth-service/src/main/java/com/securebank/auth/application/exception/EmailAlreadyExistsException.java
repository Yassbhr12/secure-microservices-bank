package com.securebank.auth.application.exception;

public final class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException() {
        super("Un compte existe déjà avec cette adresse e-mail");
    }
}
