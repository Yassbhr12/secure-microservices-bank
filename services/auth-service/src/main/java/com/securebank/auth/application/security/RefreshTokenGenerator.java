package com.securebank.auth.application.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Component
public class RefreshTokenGenerator {

    private static final int TOKEN_BYTES = 32;
    private static final String TOKEN_PREFIX = "rt_";

    private final SecureRandom secureRandom = new SecureRandom();

    public GeneratedRefreshToken generate() {
        byte[] randomBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(randomBytes);

        String randomValue = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(randomBytes);

        String rawToken = TOKEN_PREFIX + randomValue;
        String tokenHash = hash(rawToken);

        return new GeneratedRefreshToken(
            rawToken,
            tokenHash
        );
    }

    public String hash(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException(
                "rawToken is required"
            );
        }

        try {
            MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(
                rawToken.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                "SHA-256 is not available",
                exception
            );
        }
    }
}
