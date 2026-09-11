package com.securebank.auth.application.exception;

public class InvalidCredentialsException
    extends RuntimeException {

    public InvalidCredentialsException() {
        super("L'adresse e-mail ou le mot de passe est incorrect");
    }
}
