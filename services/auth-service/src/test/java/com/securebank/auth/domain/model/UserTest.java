package com.securebank.auth.domain.model;



import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    private static final String PASSWORD_HASH = "encoded-password-hash";

    @Test
    void shouldRegisterClientWithNormalizedEmailAndDefaultState() {
        User user = User.registerClient(
            " Client@Example.com ",
            PASSWORD_HASH
        );

        assertAll(
            () -> assertEquals(
                "client@example.com",
                user.getEmail()
            ),
            () -> assertEquals(
                PASSWORD_HASH,
                user.getPasswordHash()
            ),
            () -> assertEquals(Role.CLIENT, user.getRole()),
            () -> assertTrue(user.isEnabled()),
            () -> assertFalse(user.isAccountLocked()),
            () -> assertEquals(
                0,
                user.getFailedLoginAttempts()
            ),
            () -> assertNull(user.getLockedUntil())
        );
    }

    @Test
    void shouldRejectBlankEmail() {
        assertThrows(
            IllegalArgumentException.class,
            () -> User.registerClient(
                " ",
                PASSWORD_HASH
            )
        );
    }

    @Test
    void shouldRejectBlankPasswordHash() {
        assertThrows(
            IllegalArgumentException.class,
            () -> User.registerClient(
                "client@example.com",
                " "
            )
        );
    }

    @Test
    void shouldCountFailedLoginAttemptsWithoutImmediateLock() {
        User user = createUser();
        Instant now = Instant.parse("2026-09-10T14:00:00Z");

        user.recordLoginFailure(
            3,
            Duration.ofMinutes(15),
            now
        );

        assertAll(
            () -> assertEquals(
                1,
                user.getFailedLoginAttempts()
            ),
            () -> assertFalse(user.isAccountLocked()),
            () -> assertNull(user.getLockedUntil())
        );
    }

    @Test
    void shouldLockUserWhenMaximumAttemptsIsReached() {
        User user = createUser();
        Instant now = Instant.parse("2026-09-10T14:00:00Z");
        Duration lockDuration = Duration.ofMinutes(15);

        user.recordLoginFailure(3, lockDuration, now);
        user.recordLoginFailure(3, lockDuration, now);
        user.recordLoginFailure(3, lockDuration, now);

        assertAll(
            () -> assertEquals(
                3,
                user.getFailedLoginAttempts()
            ),
            () -> assertTrue(user.isAccountLocked()),
            () -> assertEquals(
                now.plus(lockDuration),
                user.getLockedUntil()
            ),
            () -> assertTrue(
                user.isTemporarilyLockedAt(
                    now.plusSeconds(300)
                )
            )
        );
    }

    @Test
    void shouldUnlockUserWhenLockPeriodHasExpired() {
        User user = createLockedUser();
        Instant unlockTime =
            Instant.parse("2026-09-10T14:16:00Z");

        user.unlockIfExpired(unlockTime);

        assertAll(
            () -> assertFalse(user.isAccountLocked()),
            () -> assertEquals(
                0,
                user.getFailedLoginAttempts()
            ),
            () -> assertNull(user.getLockedUntil())
        );
    }

    @Test
    void shouldResetSecurityStateAfterSuccessfulLogin() {
        User user = createLockedUser();

        user.recordSuccessfulLogin();

        assertAll(
            () -> assertEquals(
                0,
                user.getFailedLoginAttempts()
            ),
            () -> assertFalse(user.isAccountLocked()),
            () -> assertNull(user.getLockedUntil())
        );
    }

    @Test
    void shouldEnableAndDisableUser() {
        User user = createUser();

        user.disable();
        assertFalse(user.isEnabled());

        user.enable();
        assertTrue(user.isEnabled());
    }

    @Test
    void shouldChangeRole() {
        User user = createUser();

        user.changeRole(Role.ADMIN);

        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    void shouldRejectInvalidLockConfiguration() {
        User user = createUser();
        Instant now = Instant.parse("2026-09-10T14:00:00Z");

        assertAll(
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> user.recordLoginFailure(
                    0,
                    Duration.ofMinutes(15),
                    now
                )
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> user.recordLoginFailure(
                    3,
                    Duration.ZERO,
                    now
                )
            )
        );
    }

    private User createUser() {
        return User.registerClient(
            "client@example.com",
            PASSWORD_HASH
        );
    }

    private User createLockedUser() {
        User user = createUser();
        Instant now = Instant.parse("2026-09-10T14:00:00Z");
        Duration lockDuration = Duration.ofMinutes(15);

        user.recordLoginFailure(3, lockDuration, now);
        user.recordLoginFailure(3, lockDuration, now);
        user.recordLoginFailure(3, lockDuration, now);

        return user;
    }
}
