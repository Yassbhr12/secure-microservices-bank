package com.securebank.audit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(
    prefix = "app.security.jwt"
)
public record JwtValidationProperties(
    String issuer,
    String audience,
    Resource publicKeyLocation
) {

    public JwtValidationProperties {
        issuer = requireText(issuer, "issuer");
        audience = requireText(audience, "audience");

        if (publicKeyLocation == null) {
            throw new IllegalArgumentException(
                "publicKeyLocation is required"
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
}
