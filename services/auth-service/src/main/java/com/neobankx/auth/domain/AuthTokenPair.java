package com.neobankx.auth.domain;

import java.time.Instant;

public record AuthTokenPair(
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt,
        String tokenType
) {
}

