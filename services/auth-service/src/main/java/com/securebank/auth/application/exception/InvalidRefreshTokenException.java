package com.securebank.auth.application.exception;

public class InvalidRefreshTokenException
    extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Le refresh token est invalide ou expiré");
    }
}
