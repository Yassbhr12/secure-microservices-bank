package com.securebank.auth.application.service;

import com.securebank.auth.api.dto.LoginResponse;
import com.securebank.auth.application.exception.InvalidCredentialsException;
import com.securebank.auth.application.security.BankUserPrincipal;
import com.securebank.auth.application.security.GeneratedRefreshToken;
import com.securebank.auth.application.security.IssuedAccessToken;
import com.securebank.auth.application.security.JwtTokenService;
import com.securebank.auth.application.security.RefreshTokenGenerator;
import com.securebank.auth.config.JwtProperties;
import com.securebank.auth.domain.model.RefreshToken;
import com.securebank.auth.domain.model.User;
import com.securebank.auth.repository.RefreshTokenRepository;
import com.securebank.auth.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final JwtTokenService jwtTokenService;
    private final LoginAttemptService loginAttemptService;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    public LoginService(
        AuthenticationManager authenticationManager,
        UserRepository userRepository,
        RefreshTokenRepository refreshTokenRepository,
        RefreshTokenGenerator refreshTokenGenerator,
        JwtTokenService jwtTokenService,
        LoginAttemptService loginAttemptService,
        JwtProperties jwtProperties,
        Clock clock
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.jwtTokenService = jwtTokenService;
        this.loginAttemptService = loginAttemptService;
        this.jwtProperties = jwtProperties;
        this.clock = clock;
    }

    @Transactional
    public LoginResponse login(
        String email,
        String rawPassword
    ) {
        String normalizedEmail = normalizeEmail(email);

        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken
                    .unauthenticated(
                        normalizedEmail,
                        rawPassword
                    )
            );
        } catch (BadCredentialsException exception) {
            loginAttemptService.recordFailure(
                normalizedEmail
            );

            throw new InvalidCredentialsException();
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException();
        }

        if (!(authentication.getPrincipal()
            instanceof BankUserPrincipal principal)) {

            throw new IllegalStateException(
                "Unexpected authentication principal"
            );
        }

        User user = userRepository
            .findById(principal.getUserId())
            .orElseThrow(
                () -> new IllegalStateException(
                    "Authenticated user no longer exists"
                )
            );

        user.recordSuccessfulLogin();

        IssuedAccessToken accessToken =
            jwtTokenService.issueAccessToken(user);

        GeneratedRefreshToken generatedRefreshToken =
            refreshTokenGenerator.generate();

        Instant issuedAt = clock.instant();

        Instant familyExpiresAt = issuedAt.plus(
            jwtProperties.refreshTokenFamilyTtl()
        );

        Instant refreshTokenExpiresAt = issuedAt.plus(
            jwtProperties.refreshTokenTtl()
        );

        if (refreshTokenExpiresAt.isAfter(familyExpiresAt)) {
            refreshTokenExpiresAt = familyExpiresAt;
        }

        RefreshToken refreshToken = RefreshToken.issue(
            user,
            UUID.randomUUID(),
            generatedRefreshToken.tokenHash(),
            issuedAt,
            refreshTokenExpiresAt,
            familyExpiresAt
        );

        refreshTokenRepository.saveAndFlush(refreshToken);

        return new LoginResponse(
            "Bearer",
            accessToken.tokenValue(),
            accessToken.expiresInSeconds(),
            generatedRefreshToken.rawToken(),
            refreshTokenExpiresAt
        );
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidCredentialsException();
        }

        return email
            .trim()
            .toLowerCase(Locale.ROOT);
    }
}
