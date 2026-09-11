package com.securebank.auth.api.controller;

import com.securebank.auth.api.dto.CurrentUserResponse;
import com.securebank.auth.application.security.JwtTokenService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class CurrentUserController {

    @GetMapping("/me")
    @PreAuthorize(
        "hasAnyRole('CLIENT', 'ADMIN', 'AUDITOR', 'SECURITY_VIEWER')"
    )
    public CurrentUserResponse currentUser(
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());

        String role = jwt.getClaimAsString(
            JwtTokenService.ROLE_CLAIM
        );

        return new CurrentUserResponse(userId, role);
    }
}
