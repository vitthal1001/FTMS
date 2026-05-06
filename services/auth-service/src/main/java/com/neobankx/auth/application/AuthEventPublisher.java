package com.neobankx.auth.application;

import java.util.Map;
import java.util.UUID;

public interface AuthEventPublisher {
    void publish(String topic, UUID subject, String email, Map<String, Object> metadata);
}

