package com.securebank.account.config;

import jakarta.servlet.DispatcherType;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
@EnableConfigurationProperties(
    JwtValidationProperties.class
)
public class SecurityConfig {

    private static final String ROLE_CLAIM = "role";

    private static final Set<String> ALLOWED_ROLES =
        Set.of(
            "CLIENT",
            "ADMIN",
            "AUDITOR",
            "SECURITY_VIEWER"
        );

    @Bean
    public RSAPublicKey jwtPublicKey(
        JwtValidationProperties properties
    ) throws IOException {

        try (InputStream inputStream =
                 properties.publicKeyLocation()
                     .getInputStream()) {

            return Objects.requireNonNull(
                RsaKeyConverters.x509()
                    .convert(inputStream),
                "Unable to load JWT public key"
            );
        }
    }

    @Bean
    public JwtDecoder jwtDecoder(
        RSAPublicKey jwtPublicKey,
        JwtValidationProperties properties
    ) {
        NimbusJwtDecoder decoder =
            NimbusJwtDecoder
                .withPublicKey(jwtPublicKey)
                .build();

        OAuth2TokenValidator<Jwt> issuerValidator =
            JwtValidators.createDefaultWithIssuer(
                properties.issuer()
            );

        OAuth2TokenValidator<Jwt> audienceValidator =
            new JwtClaimValidator<List<String>>(
                JwtClaimNames.AUD,
                audiences ->
                    audiences != null
                        && audiences.contains(
                        properties.audience()
                    )
            );

        OAuth2TokenValidator<Jwt> subjectValidator =
            new JwtClaimValidator<String>(
                JwtClaimNames.SUB,
                SecurityConfig::isValidUuid
            );

        OAuth2TokenValidator<Jwt> roleValidator =
            new JwtClaimValidator<String>(
                ROLE_CLAIM,
                role ->
                    role != null
                        && ALLOWED_ROLES.contains(role)
            );

        decoder.setJwtValidator(
            new DelegatingOAuth2TokenValidator<>(
                issuerValidator,
                audienceValidator,
                subjectValidator,
                roleValidator
            )
        );

        return decoder;
    }

    @Bean
    public JwtAuthenticationConverter
    jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authorities =
            new JwtGrantedAuthoritiesConverter();

        authorities.setAuthoritiesClaimName(ROLE_CLAIM);
        authorities.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
            new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
            authorities
        );

        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        JwtAuthenticationConverter converter
    ) throws Exception {

        return http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(
                    new BearerTokenAuthenticationEntryPoint()
                )
                .accessDeniedHandler(
                    new BearerTokenAccessDeniedHandler()
                )
            )

            .authorizeHttpRequests(authorize -> authorize
                .dispatcherTypeMatchers(
                    DispatcherType.ERROR
                ).permitAll()

                .requestMatchers(
                    "/actuator/health",
                    "/actuator/health/**"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/v1/accounts"
                ).hasRole("CLIENT")

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/v1/accounts",
                    "/api/v1/accounts/**"
                ).hasRole("CLIENT")

                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/v1/admin/accounts/**"
                ).hasRole("ADMIN")


                .anyRequest().authenticated()
            )

            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(
                        converter
                    )
                )
            )

            .build();
    }

    private static boolean isValidUuid(String subject) {
        if (subject == null || subject.isBlank()) {
            return false;
        }

        try {
            UUID.fromString(subject);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
