package com.securebank.auth.api.controller;

import com.securebank.auth.application.exception.EmailAlreadyExistsException;
import com.securebank.auth.application.service.RegistrationService;
import com.securebank.auth.config.SecurityConfig;
import com.securebank.auth.domain.model.Role;
import com.securebank.auth.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistrationController.class)
@Import(SecurityConfig.class)
class RegistrationControllerTest {

    private static final String EMAIL = "client@example.com";
    private static final String PASSWORD = "MotDePasseTresFort123!";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrationService registrationService;

    @Test
    void shouldReturn201WhenRegistrationSucceeds() throws Exception {
        UUID userId = UUID.fromString(
            "52d68888-bba8-4e5d-8c93-dc5459fd4415"
        );
        Instant createdAt = Instant.parse(
            "2026-09-11T10:30:00Z"
        );

        User user = mock(User.class);

        given(user.getId()).willReturn(userId);
        given(user.getEmail()).willReturn(EMAIL);
        given(user.getRole()).willReturn(Role.CLIENT);
        given(user.isEnabled()).willReturn(true);
        given(user.getCreatedAt()).willReturn(createdAt);

        given(registrationService.register(EMAIL, PASSWORD))
            .willReturn(user);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                  "email": "client@example.com",
                                  "password": "MotDePasseTresFort123!"
                                }
                                """))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(
                MediaType.APPLICATION_JSON
            ))
            .andExpect(jsonPath("$.id").value(userId.toString()))
            .andExpect(jsonPath("$.email").value(EMAIL))
            .andExpect(jsonPath("$.role").value("CLIENT"))
            .andExpect(jsonPath("$.enabled").value(true))
            .andExpect(jsonPath("$.createdAt")
                .value(createdAt.toString()))
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(registrationService).register(EMAIL, PASSWORD);
    }

    @Test
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                  "email": "adresse-invalide",
                                  "password": "court"
                                }
                                """))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(
                MediaType.APPLICATION_PROBLEM_JSON
            ))
            .andExpect(jsonPath("$.type").value(
                "urn:secure-bank:problem:validation-error"
            ))
            .andExpect(jsonPath("$.title")
                .value("Échec de validation"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.email").isArray())
            .andExpect(jsonPath("$.errors.password").isArray());

        verifyNoInteractions(registrationService);
    }

    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        given(registrationService.register(EMAIL, PASSWORD))
            .willThrow(new EmailAlreadyExistsException());

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                  "email": "client@example.com",
                                  "password": "MotDePasseTresFort123!"
                                }
                                """))
            .andExpect(status().isConflict())
            .andExpect(content().contentTypeCompatibleWith(
                MediaType.APPLICATION_PROBLEM_JSON
            ))
            .andExpect(jsonPath("$.type").value(
                "urn:secure-bank:problem:email-already-exists"
            ))
            .andExpect(jsonPath("$.title")
                .value("Adresse e-mail déjà utilisée"))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value(
                "Un compte existe déjà avec cette adresse e-mail"
            ));

        verify(registrationService).register(EMAIL, PASSWORD);
    }
}
