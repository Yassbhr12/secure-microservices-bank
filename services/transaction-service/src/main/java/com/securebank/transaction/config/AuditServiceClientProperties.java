package com.securebank.transaction.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(
    prefix = "app.clients.audit-service"
)
public record AuditServiceClientProperties(
    URI baseUrl,
    String apiKey
) {

    public AuditServiceClientProperties {
        if (baseUrl == null) {
            throw new IllegalArgumentException(
                "Audit service base URL is required"
            );
        }

        String scheme = baseUrl.getScheme();

        if (!"http".equalsIgnoreCase(scheme)
            && !"https".equalsIgnoreCase(scheme)) {

            throw new IllegalArgumentException(
                "Audit service URL must use HTTP or HTTPS"
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
