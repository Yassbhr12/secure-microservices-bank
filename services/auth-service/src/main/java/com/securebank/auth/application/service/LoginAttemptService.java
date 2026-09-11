package com.securebank.auth.application.service;

import com.securebank.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;

@Service
public class LoginAttemptService {

    private static final int MAXIMUM_ATTEMPTS = 5;

    private static final Duration LOCK_DURATION =
        Duration.ofMinutes(15);

    private final UserRepository userRepository;
    private final Clock clock;

    public LoginAttemptService(
        UserRepository userRepository,
        Clock clock
    ) {
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String normalizedEmail) {
        userRepository
            .findByEmailIgnoreCase(normalizedEmail)
            .ifPresent(user ->
                user.recordLoginFailure(
                    MAXIMUM_ATTEMPTS,
                    LOCK_DURATION,
                    clock.instant()
                )
            );
    }
}
