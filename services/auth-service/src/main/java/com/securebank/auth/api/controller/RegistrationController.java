package com.securebank.auth.api.controller;

import com.securebank.auth.api.dto.RegisterRequest;
import com.securebank.auth.api.dto.RegisterResponse;
import com.securebank.auth.application.service.RegistrationService;
import com.securebank.auth.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping(
        value = "/register",
        // Le client doit envoyer : Content-Type: application/json
        consumes = MediaType.APPLICATION_JSON_VALUE,
        // La réponse sera retournée au format JSON.
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RegisterResponse> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        User user = registrationService.register(
            request.email(),
            request.password()
        );

        RegisterResponse response = new RegisterResponse(
            user.getId(),
            user.getEmail(),
            user.getRole(),
            user.isEnabled(),
            user.getCreatedAt()
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }
}
