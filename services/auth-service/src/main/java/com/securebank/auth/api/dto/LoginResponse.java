package com.securebank.auth.api.dto;

import java.time.Instant;
import java.util.Objects;

public record LoginResponse(
    String tokenType,
    String accessToken,
    long accessTokenExpiresIn,
    String refreshToken,
    Instant refreshTokenExpiresAt
) {

    public LoginResponse {
        requireText(tokenType, "tokenType");
        requireText(accessToken, "accessToken");
        requireText(refreshToken, "refreshToken");

        if (accessTokenExpiresIn <= 0) {
            throw new IllegalArgumentException(
                "accessTokenExpiresIn must be positive"
            );
        }

        Objects.requireNonNull(
            refreshTokenExpiresAt,
            "refreshTokenExpiresAt is required"
        );
    }

    @Override
    public String toString() {
        return "LoginResponse["
            + "tokenType=" + tokenType
            + ", accessToken=<redacted>"
            + ", accessTokenExpiresIn="
            + accessTokenExpiresIn
            + ", refreshToken=<redacted>"
            + ", refreshTokenExpiresAt="
            + refreshTokenExpiresAt
            + "]";
    }

    private static void requireText(
        String value,
        String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                fieldName + " is required"
            );
        }
    }
}
