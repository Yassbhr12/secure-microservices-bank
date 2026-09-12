package com.securebank.transaction.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(
    prefix = "app.clients.account-service"
)
public record AccountServiceClientProperties(
    URI baseUrl,
    String apiKey
) {

    public AccountServiceClientProperties {
        if (baseUrl == null) {
            throw new IllegalArgumentException(
                "Account service base URL is required"
            );
        }

        String scheme = baseUrl.getScheme();

        if (!"http".equalsIgnoreCase(scheme)
            && !"https".equalsIgnoreCase(scheme)) {

            throw new IllegalArgumentException(
                "Account service URL must use HTTP or HTTPS"
            );
        }

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
