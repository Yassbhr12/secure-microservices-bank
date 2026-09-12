package com.securebank.transaction.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtValidationProperties(
    String issuer,
    String audience,
    Resource publicKeyLocation
) {

    public JwtValidationProperties {
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException(
                "JWT issuer is required"
            );
        }

        if (audience == null || audience.isBlank()) {
            throw new IllegalArgumentException(
                "JWT audience is required"
            );
        }

        if (publicKeyLocation == null) {
            throw new IllegalArgumentException(
                "JWT public key location is required"
            );
        }

        issuer = issuer.trim();
        audience = audience.trim();
    }
}
