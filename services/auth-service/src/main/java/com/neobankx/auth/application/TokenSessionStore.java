package com.neobankx.auth.application;

import java.time.Duration;
import java.util.UUID;

public interface TokenSessionStore {
    void markFamilyActive(UUID userId, UUID familyId, Duration ttl);

    void revokeFamily(UUID userId, UUID familyId);
}

