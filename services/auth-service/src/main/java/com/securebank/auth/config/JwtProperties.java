package com.securebank.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import java.time.Duration;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
    String issuer,
    String audience,
    String keyId,
    Resource privateKeyLocation,
    Resource publicKeyLocation,
    Duration accessTokenTtl,
    Duration refreshTokenTtl,
    Duration refreshTokenFamilyTtl
) {

    public JwtProperties {
        issuer = requireText(issuer, "issuer");
        audience = requireText(audience, "audience");
        keyId = requireText(keyId, "keyId");

        if (privateKeyLocation == null) {
            throw new IllegalArgumentException(
                "privateKeyLocation is required"
            );
        }

        if (publicKeyLocation == null) {
            throw new IllegalArgumentException(
                "publicKeyLocation is required"
            );
        }

        requirePositive(accessTokenTtl, "accessTokenTtl");
        requirePositive(refreshTokenTtl, "refreshTokenTtl");
        requirePositive(
            refreshTokenFamilyTtl,
            "refreshTokenFamilyTtl"
        );

        if (accessTokenTtl.compareTo(refreshTokenTtl) >= 0) {
            throw new IllegalArgumentException(
                "accessTokenTtl must be shorter than refreshTokenTtl"
            );
        }

        if (refreshTokenTtl.compareTo(refreshTokenFamilyTtl) > 0) {
            throw new IllegalArgumentException(
                "refreshTokenTtl must not exceed refreshTokenFamilyTtl"
            );
        }
    }

    private static String requireText(
        String value,
        String propertyName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                propertyName + " is required"
            );
        }

        return value.trim();
    }

    private static void requirePositive(
        Duration duration,
        String propertyName
    ) {
        if (duration == null
            || duration.isZero()
            || duration.isNegative()) {

            throw new IllegalArgumentException(
                propertyName + " must be positive"
            );
        }
    }
}
