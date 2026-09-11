package com.securebank.auth.application.security;

import java.util.Objects;

public record GeneratedRefreshToken(
    String rawToken,
    String tokenHash
) {

    public GeneratedRefreshToken {
        Objects.requireNonNull(
            rawToken,
            "rawToken is required"
        );

        Objects.requireNonNull(
            tokenHash,
            "tokenHash is required"
        );
    }

    @Override
    public String toString() {
        return "GeneratedRefreshToken[values=<redacted>]";
    }
}
