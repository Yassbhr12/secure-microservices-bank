package com.securebank.auth.application.service;

import com.securebank.auth.api.dto.LoginResponse;
import com.securebank.auth.application.exception.InvalidRefreshTokenException;
import com.securebank.auth.application.security.GeneratedRefreshToken;
import com.securebank.auth.application.security.IssuedAccessToken;
import com.securebank.auth.application.security.JwtTokenService;
import com.securebank.auth.application.security.RefreshTokenGenerator;
import com.securebank.auth.config.JwtProperties;
import com.securebank.auth.domain.model.RefreshToken;
import com.securebank.auth.domain.model.User;
import com.securebank.auth.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class RefreshService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    public RefreshService(
        RefreshTokenRepository refreshTokenRepository,
        RefreshTokenGenerator refreshTokenGenerator,
        JwtTokenService jwtTokenService,
        JwtProperties jwtProperties,
        Clock clock
    ) {
        this.refreshTokenRepository =
            refreshTokenRepository;

        this.refreshTokenGenerator =
            refreshTokenGenerator;

        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
        this.clock = clock;
    }

    @Transactional(
        noRollbackFor = InvalidRefreshTokenException.class
    )
    public LoginResponse refresh(String rawRefreshToken) {
        String tokenHash = refreshTokenGenerator.hash(
            rawRefreshToken
        );

        RefreshToken currentToken =
            refreshTokenRepository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(
                    InvalidRefreshTokenException::new
                );

        Instant now = clock.instant();

        /*
         * Un token déjà utilisé indique un rejeu possible.
         * Toute la famille doit être révoquée.
         */
        if (currentToken.isUsed()) {
            refreshTokenRepository.revokeActiveFamily(
                currentToken.getFamilyId(),
                now
            );

            throw new InvalidRefreshTokenException();
        }

        if (currentToken.isRevoked()
            || currentToken.isExpiredAt(now)) {

            throw new InvalidRefreshTokenException();
        }

        User user = currentToken.getUser();

        if (!user.isEnabled()
            || user.isTemporarilyLockedAt(now)) {

            refreshTokenRepository.revokeActiveFamily(
                currentToken.getFamilyId(),
                now
            );

            throw new InvalidRefreshTokenException();
        }

        GeneratedRefreshToken generatedReplacement =
            refreshTokenGenerator.generate();

        Instant replacementExpiresAt = now.plus(
            jwtProperties.refreshTokenTtl()
        );

        Instant familyExpiresAt =
            currentToken.getFamilyExpiresAt();

        if (replacementExpiresAt.isAfter(
            familyExpiresAt
        )) {
            replacementExpiresAt = familyExpiresAt;
        }

        RefreshToken replacementToken =
            RefreshToken.issue(
                user,
                currentToken.getFamilyId(),
                generatedReplacement.tokenHash(),
                now,
                replacementExpiresAt,
                familyExpiresAt
            );

        /*
         * saveAndFlush garantit que le nouvel UUID existe
         * avant de relier l’ancien token à son remplaçant.
         */
        refreshTokenRepository.saveAndFlush(
            replacementToken
        );

        currentToken.consume(
            now,
            replacementToken.getId()
        );

        IssuedAccessToken accessToken =
            jwtTokenService.issueAccessToken(user);

        return new LoginResponse(
            "Bearer",
            accessToken.tokenValue(),
            accessToken.expiresInSeconds(),
            generatedReplacement.rawToken(),
            replacementExpiresAt
        );
    }
}
