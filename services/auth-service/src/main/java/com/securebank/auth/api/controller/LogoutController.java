package com.securebank.auth.api.controller;

import com.securebank.auth.api.dto.RefreshRequest;
import com.securebank.auth.application.service.LogoutService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class LogoutController {

    private final LogoutService logoutService;

    public LogoutController(
        LogoutService logoutService
    ) {
        this.logoutService = logoutService;
    }

    @PostMapping(
        value = "/logout",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> logout(
        @Valid @RequestBody RefreshRequest request
    ) {
        logoutService.logout(
            request.refreshToken()
        );

        return ResponseEntity.noContent().build();
    }
}
