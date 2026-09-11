package com.securebank.auth.application.security;

import com.securebank.auth.domain.model.User;
import com.securebank.auth.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Locale;

@Service
public class BankUserDetailsService
    implements UserDetailsService {

    private static final String INVALID_CREDENTIALS =
        "Invalid credentials";

    private final UserRepository userRepository;
    private final Clock clock;

    public BankUserDetailsService(
        UserRepository userRepository,
        Clock clock
    ) {
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        String normalizedEmail = normalizeEmail(email);

        User user = userRepository
            .findByEmailIgnoreCase(normalizedEmail)
            .orElseThrow(
                () -> new UsernameNotFoundException(
                    INVALID_CREDENTIALS
                )
            );

        return BankUserPrincipal.from(
            user,
            clock.instant()
        );
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException(
                INVALID_CREDENTIALS
            );
        }

        return email
            .trim()
            .toLowerCase(Locale.ROOT);
    }
}
