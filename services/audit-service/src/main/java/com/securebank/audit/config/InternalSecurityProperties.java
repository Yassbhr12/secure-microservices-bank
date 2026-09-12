package com.securebank.audit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
    prefix = "app.security.internal"
)
public record InternalSecurityProperties(
    String apiKey
) {

    public InternalSecurityProperties {
        if (apiKey == null
            || apiKey.isBlank()
            || apiKey.trim().length() < 32) {

            throw new IllegalArgumentException(
                "Internal API key must contain at least 32 characters"
            );
        }

        apiKey = apiKey.trim();
    }
}
