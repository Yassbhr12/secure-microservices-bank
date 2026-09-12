package com.securebank.account.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import java.util.Objects;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtValidationProperties(
    String issuer,
    String audience,
    Resource publicKeyLocation
) {

    public JwtValidationProperties {
        issuer = requireText(issuer, "issuer");
        audience = requireText(audience, "audience");

        Objects.requireNonNull(
            publicKeyLocation,
            "publicKeyLocation is required"
        );
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
}
