package com.securebank.account.api;

import com.securebank.account.api.dto.AccountResponse;
import com.securebank.account.application.AccountAdministrationService;
import com.securebank.account.application.AccountCreationService;
import com.securebank.account.application.AccountQueryService;
import com.securebank.account.domain.model.AccountStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {
    AccountController.class,
    AccountAdminController.class,
    AccountControllerSecurityTest.TestSecurityConfiguration.class
})
class AccountControllerSecurityTest {

    private static final UUID CLIENT_ID =
        UUID.fromString(
            "123e4567-e89b-12d3-a456-426614174000"
        );

    private static final UUID ACCOUNT_ID =
        UUID.fromString(
            "123e4567-e89b-12d3-a456-426614174001"
        );

    private static final Instant NOW =
        Instant.parse("2026-09-12T10:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountCreationService creationService;

    @MockitoBean
    private AccountQueryService queryService;

    @MockitoBean
    private AccountAdministrationService
        administrationService;

    @Test
    void shouldReturnUnauthorizedWithoutJwt()
        throws Exception {

        mockMvc.perform(
                post("/api/v1/accounts")
            )
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowClientToCreateAccount()
        throws Exception {

        given(creationService.createFor(CLIENT_ID))
            .willReturn(response(AccountStatus.ACTIVE));

        mockMvc.perform(
                post("/api/v1/accounts")
                    .with(jwt()
                        .jwt(token ->
                            token.subject(
                                CLIENT_ID.toString()
                            )
                        )
                        .authorities(authority(
                            "ROLE_CLIENT"
                        ))
                    )
            )
            .andExpect(status().isCreated())
            .andExpect(
                jsonPath("$.accountId")
                    .value(ACCOUNT_ID.toString())
            )
            .andExpect(
                jsonPath("$.status")
                    .value("ACTIVE")
            );
    }

    @Test
    void shouldRejectClientOnAdminEndpoint()
        throws Exception {

        mockMvc.perform(
                patch(
                    "/api/v1/admin/accounts/{id}/block",
                    ACCOUNT_ID
                )
                    .with(jwt()
                        .authorities(authority(
                            "ROLE_CLIENT"
                        ))
                    )
            )
            .andExpect(status().isForbidden());

        verifyNoInteractions(administrationService);
    }

    @Test
    void shouldAllowAdminToBlockAccount()
        throws Exception {

        given(administrationService.block(ACCOUNT_ID))
            .willReturn(
                response(AccountStatus.BLOCKED)
            );

        mockMvc.perform(
                patch(
                    "/api/v1/admin/accounts/{id}/block",
                    ACCOUNT_ID
                )
                    .with(jwt()
                        .authorities(authority(
                            "ROLE_ADMIN"
                        ))
                    )
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.status")
                    .value("BLOCKED")
            );
    }

    private static org.springframework.security.core
        .GrantedAuthority authority(String authority) {

        return new org.springframework.security.core
            .authority.SimpleGrantedAuthority(authority);
    }

    private static AccountResponse response(
        AccountStatus status
    ) {
        return new AccountResponse(
            ACCOUNT_ID,
            "SB12345678901234567890123456",
            new BigDecimal("0.00"),
            "MAD",
            status,
            NOW,
            NOW
        );
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableMethodSecurity
    static class TestSecurityConfiguration {

        @Bean
        SecurityFilterChain testSecurityFilterChain(
            HttpSecurity http
        ) throws Exception {

            return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(
                    AbstractHttpConfigurer::disable
                )
                .httpBasic(
                    AbstractHttpConfigurer::disable
                )
                .exceptionHandling(exception ->
                    exception.authenticationEntryPoint(
                        new HttpStatusEntryPoint(
                            HttpStatus.UNAUTHORIZED
                        )
                    )
                )
                .authorizeHttpRequests(authorize ->
                    authorize.anyRequest()
                        .authenticated()
                )
                .build();
        }
    }
}
