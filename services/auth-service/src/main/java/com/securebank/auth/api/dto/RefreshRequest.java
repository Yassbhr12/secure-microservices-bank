package com.securebank.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshRequest(

    @NotBlank(message = "Le refresh token est obligatoire")
    @Size(
        max = 256,
        message = "Le refresh token est invalide"
    )
    String refreshToken

) {

    @Override
    public String toString() {
        return "RefreshRequest[refreshToken=<redacted>]";
    }
}
