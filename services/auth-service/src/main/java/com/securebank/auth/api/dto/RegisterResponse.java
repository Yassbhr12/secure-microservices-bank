package com.securebank.auth.api.dto;

import com.securebank.auth.domain.model.Role;

import java.time.Instant;
import java.util.UUID;

public record RegisterResponse(
    UUID id,
    String email,
    Role role,
    boolean enabled,
    Instant createdAt
) {
}
