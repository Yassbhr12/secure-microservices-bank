package com.securebank.auth.application.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record IssuedAccessToken(
    String tokenValue,
    Instant issuedAt,
    Instant expiresAt
) {

    public IssuedAccessToken {
        if (tokenValue == null || tokenValue.isBlank()) {
            throw new IllegalArgumentException(
                "tokenValue is required"
            );
        }

        Objects.requireNonNull(
            issuedAt,
            "issuedAt is required"
        );

        Objects.requireNonNull(
            expiresAt,
            "expiresAt is required"
        );

        if (!expiresAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException(
                "expiresAt must be after issuedAt"
            );
        }
    }

    public long expiresInSeconds() {
        return Duration.between(
            issuedAt,
            expiresAt
        ).toSeconds();
    }

    @Override
    public String toString() {
        return "IssuedAccessToken["
            + "tokenValue=<redacted>, "
            + "issuedAt=" + issuedAt + ", "
            + "expiresAt=" + expiresAt
            + "]";
    }
}
