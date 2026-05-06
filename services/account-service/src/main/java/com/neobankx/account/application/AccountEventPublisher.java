package com.neobankx.account.application;

import java.util.Map;
import java.util.UUID;

public interface AccountEventPublisher {
    void publish(String topic, UUID accountId, Map<String, Object> metadata);
}

