package com.securebank.auth.application.security;

import com.securebank.auth.config.JwtProperties;
import com.securebank.auth.domain.model.Role;
import com.securebank.auth.domain.model.User;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class JwtTokenService {

    public static final String ROLE_CLAIM = "role";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;
    private final Clock clock;

    public JwtTokenService(
        JwtEncoder jwtEncoder,
        JwtProperties properties,
        Clock clock
    ) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
        this.clock = clock;
    }

    public IssuedAccessToken issueAccessToken(User user) {
        Objects.requireNonNull(user, "user is required");

        UUID userId = Objects.requireNonNull(
            user.getId(),
            "user must be persisted before issuing a token"
        );

        Role role = Objects.requireNonNull(
            user.getRole(),
            "user role is required"
        );

        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(
            properties.accessTokenTtl()
        );

        JwsHeader header = JwsHeader
            .with(SignatureAlgorithm.RS256)
            .keyId(properties.keyId())
            .type("JWT")
            .build();

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(properties.issuer())
            .subject(userId.toString())
            .audience(List.of(properties.audience()))
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .id(UUID.randomUUID().toString())
            .claim(ROLE_CLAIM, role.name())
            .build();

        Jwt encodedJwt = jwtEncoder.encode(
            JwtEncoderParameters.from(
                header,
                claims
            )
        );

        return new IssuedAccessToken(
            encodedJwt.getTokenValue(),
            issuedAt,
            expiresAt
        );
    }
}
