package com.neobankx.common.api;

import java.util.Set;
import java.util.UUID;

public record JwtPrincipal(
        UUID subject,
        String tenantId,
        Set<String> roles,
        Set<String> scopes
) {
    public JwtPrincipal {
        roles = Set.copyOf(roles == null ? Set.of() : roles);
        scopes = Set.copyOf(scopes == null ? Set.of() : scopes);
    }
}

