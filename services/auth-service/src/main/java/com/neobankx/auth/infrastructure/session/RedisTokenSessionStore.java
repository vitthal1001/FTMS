package com.neobankx.auth.infrastructure.session;

import com.neobankx.auth.application.TokenSessionStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
public class RedisTokenSessionStore implements TokenSessionStore {
    private final StringRedisTemplate redis;

    public RedisTokenSessionStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void markFamilyActive(UUID userId, UUID familyId, Duration ttl) {
        redis.opsForValue().set(key(userId, familyId), "active", ttl);
    }

    @Override
    public void revokeFamily(UUID userId, UUID familyId) {
        redis.delete(key(userId, familyId));
    }

    private String key(UUID userId, UUID familyId) {
        return "auth:session:" + userId + ":" + familyId;
    }
}

