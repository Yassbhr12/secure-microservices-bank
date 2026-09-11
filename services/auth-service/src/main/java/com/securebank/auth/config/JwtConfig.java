package com.securebank.auth.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Objects;
import java.time.Clock;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    public RSAPublicKey jwtPublicKey(
        JwtProperties properties
    ) throws IOException {

        try (InputStream inputStream =
                 properties.publicKeyLocation().getInputStream()) {

            return Objects.requireNonNull(
                RsaKeyConverters.x509().convert(inputStream),
                "Unable to load JWT public key"
            );
        }
    }

    @Bean
    public RSAPrivateKey jwtPrivateKey(
        JwtProperties properties
    ) throws IOException {

        try (InputStream inputStream =
                 properties.privateKeyLocation().getInputStream()) {

            return Objects.requireNonNull(
                RsaKeyConverters.pkcs8().convert(inputStream),
                "Unable to load JWT private key"
            );
        }
    }

    @Bean
    public JwtEncoder jwtEncoder(
        RSAPublicKey jwtPublicKey,
        RSAPrivateKey jwtPrivateKey,
        JwtProperties properties
    ) {
        RSAKey rsaKey = new RSAKey.Builder(jwtPublicKey)
            .privateKey(jwtPrivateKey)
            .keyID(properties.keyId())
            .algorithm(JWSAlgorithm.RS256)
            .build();

        JWKSource<SecurityContext> jwkSource =
            new ImmutableJWKSet<>(new JWKSet(rsaKey));

        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(
        RSAPublicKey jwtPublicKey,
        JwtProperties properties
    ) {
        NimbusJwtDecoder decoder =
            NimbusJwtDecoder.withPublicKey(jwtPublicKey).build();

        OAuth2TokenValidator<Jwt> issuerValidator =
            JwtValidators.createDefaultWithIssuer(
                properties.issuer()
            );

        OAuth2TokenValidator<Jwt> audienceValidator =
            new JwtClaimValidator<List<String>>(
                JwtClaimNames.AUD,
                audiences -> audiences != null
                    && audiences.contains(properties.audience())
            );

        decoder.setJwtValidator(
            new DelegatingOAuth2TokenValidator<>(
                issuerValidator,
                audienceValidator
            )
        );

        return decoder;
    }

    @Bean
    public Clock utcClock() {
        return Clock.systemUTC();
    }
}
