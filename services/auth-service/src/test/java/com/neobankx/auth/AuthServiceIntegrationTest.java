package com.neobankx.auth;

import com.neobankx.auth.application.AuthEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthServiceIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @MockitoBean
    AuthEventPublisher eventPublisher;

    @Test
    void registersLogsInRefreshesAndRejectsRefreshReplay() {
        RegisterRequest register = new RegisterRequest(
                "customer.one@example.com",
                "Customer One",
                "VeryStrong!2026Pass"
        );

        ResponseEntity<AuthTokenResponse> registered = post("/api/v1/auth/register", register, AuthTokenResponse.class);

        assertThat(registered.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(registered.getBody()).isNotNull();
        assertThat(registered.getBody().accessToken()).isNotBlank();
        assertThat(registered.getBody().refreshToken()).isNotBlank();
        verify(eventPublisher).publish(eq("user-registered"), any(), eq("customer.one@example.com"), any());

        LoginRequest login = new LoginRequest("customer.one@example.com", "VeryStrong!2026Pass");
        ResponseEntity<AuthTokenResponse> loggedIn = post("/api/v1/auth/login", login, AuthTokenResponse.class);

        assertThat(loggedIn.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loggedIn.getBody()).isNotNull();
        verify(eventPublisher).publish(eq("user-login"), any(), eq("customer.one@example.com"), any());

        String refreshToken = loggedIn.getBody().refreshToken();
        ResponseEntity<AuthTokenResponse> refreshed = post("/api/v1/auth/refresh", new RefreshTokenRequest(refreshToken), AuthTokenResponse.class);

        assertThat(refreshed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refreshed.getBody()).isNotNull();
        assertThat(refreshed.getBody().refreshToken()).isNotEqualTo(refreshToken);
        verify(eventPublisher).publish(eq("token-refreshed"), any(), eq("customer.one@example.com"), any());

        ResponseEntity<String> replay = post("/api/v1/auth/refresh", new RefreshTokenRequest(refreshToken), String.class);

        assertThat(replay.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(replay.getBody()).contains("REFRESH_TOKEN_REPLAYED");
        verify(eventPublisher).publish(eq("auth-failed"), any(), eq("customer.one@example.com"), any());
    }

    @Test
    void exposesOpenApiAndHealthEndpoints() {
        ResponseEntity<String> health = rest.getForEntity("http://localhost:" + port + "/actuator/health", String.class);
        ResponseEntity<String> openApi = rest.getForEntity("http://localhost:" + port + "/v3/api-docs", String.class);

        assertThat(health.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(openApi.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(openApi.getBody()).contains("NeoBankX Auth Service");
    }

    private <T> ResponseEntity<T> post(String path, Object request, Class<T> responseType) {
        return rest.postForEntity("http://localhost:" + port + path, new HttpEntity<>(request), responseType);
    }

    record RegisterRequest(String email, String fullName, String password) {
    }

    record LoginRequest(String email, String password) {
    }

    record RefreshTokenRequest(String refreshToken) {
    }

    record AuthTokenResponse(
            String accessToken,
            Instant accessTokenExpiresAt,
            String refreshToken,
            Instant refreshTokenExpiresAt,
            String tokenType
    ) {
    }
}
