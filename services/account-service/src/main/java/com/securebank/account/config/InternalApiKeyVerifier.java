package com.securebank.account.config;

import com.securebank.account.exception.InvalidInternalCredentialsException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class InternalApiKeyVerifier {

    private final byte[] expectedApiKey;

    public InternalApiKeyVerifier(
        InternalSecurityProperties properties
    ) {
        this.expectedApiKey = properties
            .apiKey()
            .getBytes(StandardCharsets.UTF_8);
    }

    public void verify(String providedApiKey) {
        if (providedApiKey == null) {
            throw new InvalidInternalCredentialsException();
        }

        byte[] providedBytes = providedApiKey
            .getBytes(StandardCharsets.UTF_8);

        if (!MessageDigest.isEqual(
            expectedApiKey,
            providedBytes
        )) {
            throw new InvalidInternalCredentialsException();
        }
    }
}
