package com.neobankx.account.infrastructure.events;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AccountEvent(
        UUID eventId,
        String type,
        UUID accountId,
        Instant occurredAt,
        String correlationId,
        Map<String, Object> metadata
) {
}

