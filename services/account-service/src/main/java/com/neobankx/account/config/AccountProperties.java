package com.neobankx.account.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Validated
@ConfigurationProperties(prefix = "neobankx.account")
public record AccountProperties(
        @Valid @NotNull Jwt jwt,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String defaultCurrency,
        @NotNull UUID systemOpeningAccountId
) {
    public record Jwt(
            @NotBlank String issuer,
            @NotBlank @Size(min = 32) String secret
    ) {
    }
}

