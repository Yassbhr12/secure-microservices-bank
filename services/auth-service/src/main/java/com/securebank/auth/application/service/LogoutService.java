package com.securebank.auth.application.service;

import com.securebank.auth.application.security.RefreshTokenGenerator;
import com.securebank.auth.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class LogoutService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final Clock clock;

    public LogoutService(
        RefreshTokenRepository refreshTokenRepository,
        RefreshTokenGenerator refreshTokenGenerator,
        Clock clock
    ) {
        this.refreshTokenRepository =
            refreshTokenRepository;

        this.refreshTokenGenerator =
            refreshTokenGenerator;

        this.clock = clock;
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        String tokenHash = refreshTokenGenerator.hash(
            rawRefreshToken
        );

        refreshTokenRepository
            .findByTokenHashForUpdate(tokenHash)
            .ifPresent(token -> {
                Instant revokedAt = clock.instant();

                refreshTokenRepository.revokeActiveFamily(
                    token.getFamilyId(),
                    revokedAt
                );
            });
    }
}
