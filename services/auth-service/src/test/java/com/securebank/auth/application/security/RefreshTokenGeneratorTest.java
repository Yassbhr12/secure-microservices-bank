package com.securebank.auth.application.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenGeneratorTest {

    private final RefreshTokenGenerator generator =
        new RefreshTokenGenerator();

    @Test
    void shouldGenerateSecureUrlSafeTokenAndHash() {
        GeneratedRefreshToken generated =
            generator.generate();

        assertNotNull(generated.rawToken());
        assertNotNull(generated.tokenHash());

        assertTrue(
            generated.rawToken().matches(
                "^rt_[A-Za-z0-9_-]{43}$"
            )
        );

        assertTrue(
            generated.tokenHash().matches(
                "^[0-9a-f]{64}$"
            )
        );

        assertEquals(
            generator.hash(generated.rawToken()),
            generated.tokenHash()
        );

        assertFalse(generated.rawToken().contains("="));
    }

    @Test
    void shouldGenerateDifferentTokens() {
        GeneratedRefreshToken first =
            generator.generate();

        GeneratedRefreshToken second =
            generator.generate();

        assertNotEquals(
            first.rawToken(),
            second.rawToken()
        );

        assertNotEquals(
            first.tokenHash(),
            second.tokenHash()
        );
    }

    @Test
    void shouldNeverExposeTokenThroughToString() {
        GeneratedRefreshToken generated =
            generator.generate();

        String representation = generated.toString();

        assertFalse(
            representation.contains(generated.rawToken())
        );

        assertFalse(
            representation.contains(generated.tokenHash())
        );
    }

    @Test
    void shouldRejectBlankTokenWhenHashing() {
        assertThrows(
            IllegalArgumentException.class,
            () -> generator.hash(" ")
        );
    }
}
