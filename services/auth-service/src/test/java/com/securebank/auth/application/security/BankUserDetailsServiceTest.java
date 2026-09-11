package com.securebank.auth.application.security;

import com.securebank.auth.domain.model.Role;
import com.securebank.auth.domain.model.User;
import com.securebank.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BankUserDetailsServiceTest {

    private static final Instant NOW =
        Instant.parse("2026-09-11T18:00:00Z");

    @Test
    void shouldLoadUserAndCreatePrincipal() {
        UserRepository repository =
            mock(UserRepository.class);

        User user = mock(User.class);
        UUID userId = UUID.randomUUID();

        when(user.getId()).thenReturn(userId);
        when(user.getEmail())
            .thenReturn("client@example.com");
        when(user.getPasswordHash())
            .thenReturn("$2a$12$hashed-password");
        when(user.getRole()).thenReturn(Role.CLIENT);
        when(user.isEnabled()).thenReturn(true);
        when(user.isTemporarilyLockedAt(NOW))
            .thenReturn(false);

        when(repository.findByEmailIgnoreCase(
            "client@example.com"
        )).thenReturn(Optional.of(user));

        BankUserDetailsService service =
            new BankUserDetailsService(
                repository,
                Clock.fixed(NOW, ZoneOffset.UTC)
            );

        BankUserPrincipal principal =
            (BankUserPrincipal)
                service.loadUserByUsername(
                    " CLIENT@EXAMPLE.COM "
                );

        assertEquals(userId, principal.getUserId());
        assertEquals(
            "client@example.com",
            principal.getUsername()
        );
        assertEquals(Role.CLIENT, principal.getRole());
        assertTrue(principal.isEnabled());
        assertTrue(principal.isAccountNonLocked());

        assertTrue(
            principal.getAuthorities()
                .stream()
                .anyMatch(authority ->
                    authority.getAuthority()
                        .equals("ROLE_CLIENT")
                )
        );

        verify(repository).findByEmailIgnoreCase(
            "client@example.com"
        );
    }

    @Test
    void shouldRejectUnknownEmail() {
        UserRepository repository =
            mock(UserRepository.class);

        when(repository.findByEmailIgnoreCase(
            "unknown@example.com"
        )).thenReturn(Optional.empty());

        BankUserDetailsService service =
            new BankUserDetailsService(
                repository,
                Clock.fixed(NOW, ZoneOffset.UTC)
            );

        assertThrows(
            UsernameNotFoundException.class,
            () -> service.loadUserByUsername(
                "unknown@example.com"
            )
        );
    }

    @Test
    void shouldExposeTemporaryLockToSpringSecurity() {
        UserRepository repository =
            mock(UserRepository.class);

        User user = mock(User.class);

        when(user.getId()).thenReturn(UUID.randomUUID());
        when(user.getEmail())
            .thenReturn("locked@example.com");
        when(user.getPasswordHash())
            .thenReturn("$2a$12$hashed-password");
        when(user.getRole()).thenReturn(Role.CLIENT);
        when(user.isEnabled()).thenReturn(true);
        when(user.isTemporarilyLockedAt(NOW))
            .thenReturn(true);

        when(repository.findByEmailIgnoreCase(
            "locked@example.com"
        )).thenReturn(Optional.of(user));

        BankUserDetailsService service =
            new BankUserDetailsService(
                repository,
                Clock.fixed(NOW, ZoneOffset.UTC)
            );

        BankUserPrincipal principal =
            (BankUserPrincipal)
                service.loadUserByUsername(
                    "locked@example.com"
                );

        assertFalse(principal.isAccountNonLocked());
    }
}
