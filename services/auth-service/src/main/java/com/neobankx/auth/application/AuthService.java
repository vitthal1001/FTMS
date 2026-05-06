package com.neobankx.auth.application;

import com.neobankx.auth.config.AuthProperties;
import com.neobankx.auth.domain.AuthRole;
import com.neobankx.auth.domain.AuthTokenPair;
import com.neobankx.auth.infrastructure.persistence.RefreshTokenEntity;
import com.neobankx.auth.infrastructure.persistence.RefreshTokenRepository;
import com.neobankx.auth.infrastructure.persistence.UserAccountEntity;
import com.neobankx.auth.infrastructure.persistence.UserAccountRepository;
import com.neobankx.auth.infrastructure.security.JwtTokenIssuer;
import com.neobankx.auth.infrastructure.security.RefreshTokenGenerator;
import com.neobankx.auth.infrastructure.security.TokenHashing;
import com.neobankx.common.api.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserAccountRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    private final JwtTokenIssuer jwtTokenIssuer;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final TokenSessionStore tokenSessionStore;
    private final AuthEventPublisher events;
    private final AuthProperties properties;
    private final Clock clock;

    public AuthService(
            UserAccountRepository users,
            RefreshTokenRepository refreshTokens,
            PasswordEncoder passwordEncoder,
            PasswordPolicy passwordPolicy,
            JwtTokenIssuer jwtTokenIssuer,
            RefreshTokenGenerator refreshTokenGenerator,
            TokenSessionStore tokenSessionStore,
            AuthEventPublisher events,
            AuthProperties properties,
            Clock clock
    ) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicy = passwordPolicy;
        this.jwtTokenIssuer = jwtTokenIssuer;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.tokenSessionStore = tokenSessionStore;
        this.events = events;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public AuthTokenPair register(String email, String fullName, String password) {
        String normalizedEmail = normalizeEmail(email);
        passwordPolicy.validate(password, normalizedEmail);
        if (users.existsByEmail(normalizedEmail)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED", "Email is already registered");
        }

        UserAccountEntity user = new UserAccountEntity(
                normalizedEmail,
                fullName.trim(),
                passwordEncoder.encode(password),
                Set.of(AuthRole.CUSTOMER)
        );
        users.save(user);
        AuthTokenPair tokens = issueTokenFamily(user);
        events.publish("user-registered", user.getId(), user.getEmail(), Map.of("roles", user.roleNames()));
        log.info("user_registered userId={}", user.getId());
        return tokens;
    }

    @Transactional
    public AuthTokenPair login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        UserAccountEntity user = users.findByEmail(normalizedEmail)
                .orElseThrow(() -> failedAuth(null, normalizedEmail, "Unknown email"));
        Instant now = clock.instant();

        if (!user.isEnabled() || user.isLocked(now)) {
            events.publish("auth-failed", user.getId(), user.getEmail(), Map.of("reason", "locked_or_disabled"));
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", "Invalid credentials");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            user.recordFailedLogin(properties.lockout().maxFailedAttempts(), properties.lockout().duration(), now);
            events.publish("auth-failed", user.getId(), user.getEmail(), Map.of("reason", "bad_credentials", "failedAttempts", user.getFailedLoginAttempts()));
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", "Invalid credentials");
        }

        user.recordSuccessfulLogin(now);
        AuthTokenPair tokens = issueTokenFamily(user);
        events.publish("user-login", user.getId(), user.getEmail(), Map.of("roles", user.roleNames()));
        return tokens;
    }

    @Transactional
    public AuthTokenPair refresh(String refreshToken) {
        String tokenHash = TokenHashing.sha256(refreshToken);
        RefreshTokenEntity current = refreshTokens.findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> failedAuth(null, null, "Refresh token not found"));
        UserAccountEntity user = current.getUser();
        Instant now = clock.instant();

        if (current.isReplayAttempt(now)) {
            refreshTokens.revokeFamily(current.getFamilyId(), now);
            tokenSessionStore.revokeFamily(user.getId(), current.getFamilyId());
            events.publish("auth-failed", user.getId(), user.getEmail(), Map.of("reason", "refresh_replay", "familyId", current.getFamilyId().toString()));
            throw new ApiException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_REPLAYED", "Refresh token cannot be reused");
        }

        if (current.isExpired(now)) {
            current.revoke(now);
            events.publish("auth-failed", user.getId(), user.getEmail(), Map.of("reason", "refresh_expired"));
            throw new ApiException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED", "Refresh token expired");
        }

        current.markUsed(now);
        AuthTokenPair tokens = issueToken(user, current.getFamilyId());
        events.publish("token-refreshed", user.getId(), user.getEmail(), Map.of("familyId", current.getFamilyId().toString()));
        return tokens;
    }

    @Transactional
    public void logout(String refreshToken) {
        String tokenHash = TokenHashing.sha256(refreshToken);
        refreshTokens.findByTokenHashForUpdate(tokenHash).ifPresent(token -> {
            Instant now = clock.instant();
            token.revoke(now);
            refreshTokens.revokeFamily(token.getFamilyId(), now);
            tokenSessionStore.revokeFamily(token.getUser().getId(), token.getFamilyId());
        });
    }

    private AuthTokenPair issueTokenFamily(UserAccountEntity user) {
        return issueToken(user, UUID.randomUUID());
    }

    private AuthTokenPair issueToken(UserAccountEntity user, UUID familyId) {
        Instant now = clock.instant();
        Instant accessExpiresAt = now.plus(properties.jwt().accessTokenTtl());
        Instant refreshExpiresAt = now.plus(properties.refresh().tokenTtl());
        String accessToken = jwtTokenIssuer.issue(user.getId(), user.getEmail(), user.roleNames(), now, accessExpiresAt);
        String refreshToken = refreshTokenGenerator.generate();
        RefreshTokenEntity entity = new RefreshTokenEntity(
                user,
                familyId,
                TokenHashing.sha256(refreshToken),
                now,
                refreshExpiresAt
        );
        refreshTokens.save(entity);
        tokenSessionStore.markFamilyActive(user.getId(), familyId, properties.refresh().tokenTtl());
        return new AuthTokenPair(accessToken, accessExpiresAt, refreshToken, refreshExpiresAt, "Bearer");
    }

    private ApiException failedAuth(UUID subject, String email, String reason) {
        events.publish("auth-failed", subject, email, Map.of("reason", reason));
        return new ApiException(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", "Invalid credentials");
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}

