package com.securebank.auth.repository;


import com.securebank.auth.domain.model.Role;
import com.securebank.auth.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)
class UserRepositoryTest {

    private static final String PASSWORD_HASH =
        "encoded-password-hash";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES =
        new PostgreSQLContainer("postgres:17");

    private final UserRepository userRepository;

    @Autowired
    UserRepositoryTest(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Test
    void shouldSaveAndFindUserIgnoringEmailCase() {
        User user = User.registerClient(
            "Client@Example.com",
            PASSWORD_HASH
        );

        User savedUser = userRepository.saveAndFlush(user);

        Optional<User> result =
            userRepository.findByEmailIgnoreCase(
                "CLIENT@EXAMPLE.COM"
            );

        assertAll(
            () -> assertTrue(result.isPresent()),
            () -> assertEquals(
                savedUser.getId(),
                result.orElseThrow().getId()
            ),
            () -> assertEquals(
                "client@example.com",
                result.orElseThrow().getEmail()
            ),
            () -> assertEquals(
                Role.CLIENT,
                result.orElseThrow().getRole()
            ),
            () -> assertNotNull(savedUser.getId()),
            () -> assertNotNull(savedUser.getCreatedAt()),
            () -> assertNotNull(savedUser.getUpdatedAt())
        );
    }

    @Test
    void shouldCheckEmailExistenceIgnoringCase() {
        User user = User.registerClient(
            "client@example.com",
            PASSWORD_HASH
        );

        userRepository.saveAndFlush(user);

        assertAll(
            () -> assertTrue(
                userRepository.existsByEmailIgnoreCase(
                    "CLIENT@EXAMPLE.COM"
                )
            ),
            () -> assertFalse(
                userRepository.existsByEmailIgnoreCase(
                    "unknown@example.com"
                )
            )
        );
    }

    @Test
    void shouldRejectDuplicateEmail() {
        User firstUser = User.registerClient(
            "client@example.com",
            PASSWORD_HASH
        );

        User secondUser = User.registerClient(
            "CLIENT@EXAMPLE.COM",
            PASSWORD_HASH
        );

        userRepository.saveAndFlush(firstUser);

        assertThrows(
            DataIntegrityViolationException.class,
            () -> userRepository.saveAndFlush(secondUser)
        );
    }

    @Test
    void shouldUpdateTimestampAndVersionWhenUserChanges() {
        User user = User.registerClient(
            "client@example.com",
            PASSWORD_HASH
        );

        User savedUser = userRepository.saveAndFlush(user);

        Instant initialCreatedAt = savedUser.getCreatedAt();
        Instant initialUpdatedAt = savedUser.getUpdatedAt();
        long initialVersion = savedUser.getVersion();

        savedUser.disable();
        userRepository.saveAndFlush(savedUser);

        assertAll(
            () -> assertFalse(savedUser.isEnabled()),
            () -> assertEquals(
                initialCreatedAt,
                savedUser.getCreatedAt()
            ),
            () -> assertFalse(
                savedUser.getUpdatedAt()
                    .isBefore(initialUpdatedAt)
            ),
            () -> assertTrue(
                savedUser.getVersion() > initialVersion
            )
        );
    }
}
