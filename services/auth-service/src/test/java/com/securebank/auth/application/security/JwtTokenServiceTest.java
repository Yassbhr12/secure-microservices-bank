package com.securebank.auth.application.security;

import com.securebank.auth.config.JwtProperties;
import com.securebank.auth.domain.model.Role;
import com.securebank.auth.domain.model.User;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtTokenServiceTest {

    private static final Instant NOW =
        Instant.parse("2026-09-11T18:00:00Z");

    @Test
    void shouldIssueAccessTokenWithExpectedClaims() {
        JwtEncoder jwtEncoder = mock(JwtEncoder.class);
        User user = mock(User.class);

        UUID userId = UUID.randomUUID();

        when(user.getId()).thenReturn(userId);
        when(user.getRole()).thenReturn(Role.CLIENT);

        Jwt encodedJwt = Jwt
            .withTokenValue("signed.jwt.value")
            .header("alg", "RS256")
            .claim("sub", userId.toString())
            .issuedAt(NOW)
            .expiresAt(NOW.plusSeconds(600))
            .build();

        when(jwtEncoder.encode(any()))
            .thenReturn(encodedJwt);

        JwtProperties properties = createProperties();

        JwtTokenService service = new JwtTokenService(
            jwtEncoder,
            properties,
            Clock.fixed(NOW, ZoneOffset.UTC)
        );

        IssuedAccessToken result =
            service.issueAccessToken(user);

        assertEquals(
            "signed.jwt.value",
            result.tokenValue()
        );

        assertEquals(NOW, result.issuedAt());

        assertEquals(
            NOW.plusSeconds(600),
            result.expiresAt()
        );

        assertEquals(600, result.expiresInSeconds());

        ArgumentCaptor<JwtEncoderParameters> captor =
            ArgumentCaptor.forClass(
                JwtEncoderParameters.class
            );

        verify(jwtEncoder).encode(captor.capture());

        JwtEncoderParameters parameters =
            captor.getValue();

        assertEquals(
            "auth-key-2026-01",
            parameters.getJwsHeader().getKeyId()
        );

        assertEquals(
            "JWT",
            parameters.getJwsHeader().getType()
        );

        assertEquals(
            "secure-bank-auth-service",
            parameters.getClaims().getClaim("iss")
        );

        assertEquals(
            userId.toString(),
            parameters.getClaims().getSubject()
        );

        assertEquals(
            "CLIENT",
            parameters.getClaims().getClaim("role")
        );

        assertEquals(
            NOW,
            parameters.getClaims().getIssuedAt()
        );

        assertEquals(
            NOW.plusSeconds(600),
            parameters.getClaims().getExpiresAt()
        );

        assertNotNull(parameters.getClaims().getId());

        assertTrue(
            parameters.getClaims()
                .getAudience()
                .contains("secure-bank-api")
        );
    }

    @Test
    void shouldRejectUserWithoutPersistedId() {
        JwtEncoder jwtEncoder = mock(JwtEncoder.class);
        User user = mock(User.class);

        when(user.getId()).thenReturn(null);

        JwtTokenService service = new JwtTokenService(
            jwtEncoder,
            createProperties(),
            Clock.fixed(NOW, ZoneOffset.UTC)
        );

        assertThrows(
            NullPointerException.class,
            () -> service.issueAccessToken(user)
        );

        verifyNoInteractions(jwtEncoder);
    }

    private JwtProperties createProperties() {
        ByteArrayResource fakeKey =
            new ByteArrayResource(new byte[]{1});

        return new JwtProperties(
            "secure-bank-auth-service",
            "secure-bank-api",
            "auth-key-2026-01",
            fakeKey,
            fakeKey,
            Duration.ofMinutes(10),
            Duration.ofDays(7),
            Duration.ofDays(30)
        );
    }
}
