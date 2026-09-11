package com.securebank.auth.config;

import com.securebank.auth.application.security.BankUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration(proxyBeanMethods = false)
public class AuthenticationConfig {

    @Bean
    public DaoAuthenticationProvider
    daoAuthenticationProvider(
        BankUserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider =
            new DaoAuthenticationProvider(
                userDetailsService
            );

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
        DaoAuthenticationProvider provider
    ) {
        return new ProviderManager(
            List.of(provider)
        );
    }
}
