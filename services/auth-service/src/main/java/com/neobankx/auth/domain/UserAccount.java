package com.neobankx.auth.domain;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserAccount(
        UUID id,
        String email,
        String fullName,
        Set<AuthRole> roles,
        boolean enabled,
        int failedLoginAttempts,
        Instant lockedUntil
) {
    public boolean isLocked(Instant now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }
}

