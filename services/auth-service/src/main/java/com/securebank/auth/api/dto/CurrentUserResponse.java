package com.securebank.auth.api.dto;

import java.util.UUID;

public record CurrentUserResponse(
    UUID userId,
    String role
) {
}
