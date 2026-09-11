package com.securebank.auth.application.service;

import com.securebank.auth.application.exception.EmailAlreadyExistsException;
import com.securebank.auth.domain.model.Role;
import com.securebank.auth.domain.model.User;
import com.securebank.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    private static final String RAW_PASSWORD = "MotDePasseTresFort123!";
    private static final String PASSWORD_HASH = "hashed-password";
    private static final String NORMALIZED_EMAIL = "client@example.com";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationService(
            userRepository,
            passwordEncoder
        );
    }

    @Test
    void shouldRegisterANewClient() {
        when(passwordEncoder.encode(RAW_PASSWORD))
            .thenReturn(PASSWORD_HASH);

        when(userRepository.existsByEmailIgnoreCase(NORMALIZED_EMAIL))
            .thenReturn(false);

        when(userRepository.saveAndFlush(any(User.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        User result = registrationService.register(
            " Client@Example.com ",
            RAW_PASSWORD
        );

        assertThat(result.getEmail()).isEqualTo(NORMALIZED_EMAIL);
        assertThat(result.getPasswordHash()).isEqualTo(PASSWORD_HASH);
        assertThat(result.getPasswordHash()).isNotEqualTo(RAW_PASSWORD);
        assertThat(result.getRole()).isEqualTo(Role.CLIENT);
        assertThat(result.isEnabled()).isTrue();

        verify(passwordEncoder).encode(RAW_PASSWORD);
        verify(userRepository).existsByEmailIgnoreCase(NORMALIZED_EMAIL);
        verify(userRepository).saveAndFlush(any(User.class));
    }

    @Test
    void shouldRejectAnAlreadyExistingEmail() {
        when(passwordEncoder.encode(RAW_PASSWORD))
            .thenReturn(PASSWORD_HASH);

        when(userRepository.existsByEmailIgnoreCase(NORMALIZED_EMAIL))
            .thenReturn(true);

        assertThatThrownBy(() ->
            registrationService.register(
                "CLIENT@EXAMPLE.COM",
                RAW_PASSWORD
            )
        )
            .isInstanceOf(EmailAlreadyExistsException.class)
            .hasMessage("Un compte existe déjà avec cette adresse e-mail");

        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void shouldTranslateDatabaseUniqueConstraintViolation() {
        when(passwordEncoder.encode(RAW_PASSWORD))
            .thenReturn(PASSWORD_HASH);

        when(userRepository.existsByEmailIgnoreCase(NORMALIZED_EMAIL))
            .thenReturn(false);

        when(userRepository.saveAndFlush(any(User.class)))
            .thenThrow(new DataIntegrityViolationException(
                "Duplicate email"
            ));

        assertThatThrownBy(() ->
            registrationService.register(
                NORMALIZED_EMAIL,
                RAW_PASSWORD
            )
        )
            .isInstanceOf(EmailAlreadyExistsException.class);
    }
}
