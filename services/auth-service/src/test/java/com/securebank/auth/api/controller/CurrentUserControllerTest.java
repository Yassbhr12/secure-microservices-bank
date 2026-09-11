package com.securebank.auth.api.controller;

import com.securebank.auth.application.security.JwtTokenService;
import com.securebank.auth.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CurrentUserController.class)
@ContextConfiguration(classes = {
    CurrentUserController.class,
    SecurityConfig.class
})
class CurrentUserControllerTest {

    private static final UUID USER_ID =
        UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldReturnCurrentUserForValidClientToken()
        throws Exception {

        given(jwtDecoder.decode("valid-client-token"))
            .willReturn(createJwt("valid-client-token", "CLIENT"));

        mockMvc.perform(get("/api/v1/auth/me")
                .header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer valid-client-token"
                ))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.userId").value(USER_ID.toString())
            )
            .andExpect(
                jsonPath("$.role").value("CLIENT")
            );
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsMissing()
        throws Exception {

        mockMvc.perform(get("/api/v1/auth/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsInvalid()
        throws Exception {

        given(jwtDecoder.decode("invalid-token"))
            .willThrow(
                new BadJwtException("Invalid JWT")
            );

        mockMvc.perform(get("/api/v1/auth/me")
                .header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer invalid-token"
                ))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnForbiddenWhenRoleIsNotAllowed()
        throws Exception {

        given(jwtDecoder.decode("unsupported-role-token"))
            .willReturn(
                createJwt(
                    "unsupported-role-token",
                    "UNKNOWN"
                )
            );

        mockMvc.perform(get("/api/v1/auth/me")
                .header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer unsupported-role-token"
                ))
            .andExpect(status().isForbidden());
    }

    private Jwt createJwt(
        String tokenValue,
        String role
    ) {
        Instant issuedAt =
            Instant.parse("2026-09-11T12:00:00Z");

        return Jwt.withTokenValue(tokenValue)
            .header("alg", "RS256")
            .subject(USER_ID.toString())
            .claim(JwtTokenService.ROLE_CLAIM, role)
            .issuedAt(issuedAt)
            .expiresAt(issuedAt.plusSeconds(600))
            .build();
    }
}
