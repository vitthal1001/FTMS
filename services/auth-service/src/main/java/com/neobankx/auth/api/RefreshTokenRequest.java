package com.neobankx.auth.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshTokenRequest(
        @NotBlank @Size(min = 43, max = 256) String refreshToken
) {
}

