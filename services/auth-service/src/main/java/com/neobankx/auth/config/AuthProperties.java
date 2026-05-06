package com.neobankx.auth.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "neobankx.auth")
public record AuthProperties(
        @Valid @NotNull Jwt jwt,
        @Valid @NotNull Refresh refresh,
        @Valid @NotNull Lockout lockout
) {
    public record Jwt(
            @NotBlank String issuer,
            @NotBlank @Size(min = 32) String secret,
            @NotNull Duration accessTokenTtl
    ) {
    }

    public record Refresh(
            @NotNull Duration tokenTtl
    ) {
    }

    public record Lockout(
            @Min(3) int maxFailedAttempts,
            @NotNull Duration duration
    ) {
    }
}

