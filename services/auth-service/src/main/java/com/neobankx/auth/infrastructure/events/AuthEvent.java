package com.neobankx.auth.infrastructure.events;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuthEvent(
        UUID eventId,
        String type,
        UUID subject,
        String email,
        Instant occurredAt,
        String correlationId,
        Map<String, Object> metadata
) {
}

