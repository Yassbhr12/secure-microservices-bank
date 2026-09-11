package com.securebank.auth.api.controller;

import com.securebank.auth.api.dto.LoginRequest;
import com.securebank.auth.api.dto.LoginResponse;
import com.securebank.auth.application.service.LoginService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping(
        value = "/login",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LoginResponse> login(
        @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = loginService.login(
            request.email(),
            request.password()
        );

        return ResponseEntity.ok(response);
    }
}
