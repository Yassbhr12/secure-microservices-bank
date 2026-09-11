package com.securebank.auth.api.controller;

import com.securebank.auth.api.dto.LoginResponse;
import com.securebank.auth.api.dto.RefreshRequest;
import com.securebank.auth.application.service.RefreshService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class RefreshController {

    private final RefreshService refreshService;

    public RefreshController(
        RefreshService refreshService
    ) {
        this.refreshService = refreshService;
    }

    @PostMapping(
        value = "/refresh",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LoginResponse> refresh(
        @Valid @RequestBody RefreshRequest request
    ) {
        LoginResponse response = refreshService.refresh(
            request.refreshToken()
        );

        return ResponseEntity.ok(response);
    }
}
