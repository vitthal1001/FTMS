package com.neobankx.account;

import com.neobankx.account.application.AccountEventPublisher;
import com.neobankx.account.domain.AccountProductType;
import com.neobankx.account.domain.AccountStatus;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountServiceIntegrationTest {
    private static final String JWT_SECRET = "test-jwt-secret-must-be-long-enough-32";
    private static final String CUSTOMER_SUBJECT = "11111111-1111-1111-1111-111111111111";
    private static final String ADMIN_SUBJECT = "22222222-2222-2222-2222-222222222222";

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @MockitoBean
    AccountEventPublisher eventPublisher;

    @Test
    void createsAccountIdempotentlyTransfersAtomicallyAndBlocksFrozenDebits() {
        AccountResponse source = createAccount("idem-create-source", new CreateAccountRequest(
                "USD",
                AccountProductType.CHECKING,
                new BigDecimal("125.00"),
                null
        ), customerToken()).getBody();

        assertThat(source).isNotNull();
        assertThat(source.ownerSubject()).isEqualTo(CUSTOMER_SUBJECT);
        assertThat(source.status()).isEqualTo(AccountStatus.ACTIVE);
        verify(eventPublisher).publish(eq("account-created"), eq(source.id()), any());
        verify(eventPublisher).publish(eq("balance-updated"), eq(source.id()), any());

        ResponseEntity<AccountResponse> replay = createAccount("idem-create-source", new CreateAccountRequest(
                "USD",
                AccountProductType.CHECKING,
                new BigDecimal("125.00"),
                null
        ), customerToken());
        assertThat(replay.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(replay.getBody()).isNotNull();
        assertThat(replay.getBody().id()).isEqualTo(source.id());

        AccountResponse target = createAccount("idem-create-target", new CreateAccountRequest(
                "USD",
                AccountProductType.SAVINGS,
                BigDecimal.ZERO,
                null
        ), customerToken()).getBody();
        assertThat(target).isNotNull();

        TransferResponse transfer = transfer("idem-transfer-1", new TransferRequest(
                source.id(),
                target.id(),
                new BigDecimal("25.00"),
                "USD",
                "Move funds to savings"
        ), customerToken()).getBody();
        assertThat(transfer).isNotNull();
        assertThat(transfer.amount()).isEqualByComparingTo("25.00");

        BalanceResponse sourceBalance = getBalance(source.id(), customerToken()).getBody();
        BalanceResponse targetBalance = getBalance(target.id(), customerToken()).getBody();
        assertThat(sourceBalance).isNotNull();
        assertThat(targetBalance).isNotNull();
        assertThat(sourceBalance.availableBalance()).isEqualByComparingTo("100.00");
        assertThat(targetBalance.availableBalance()).isEqualByComparingTo("25.00");

        LedgerEntryResponse[] sourceLedger = getLedger(source.id(), customerToken()).getBody();
        assertThat(sourceLedger).isNotNull();
        assertThat(sourceLedger).hasSize(2);
        assertThat(List.of(sourceLedger).stream().map(LedgerEntryResponse::signedAmount))
                .contains(new BigDecimal("-25.00"), new BigDecimal("125.00"));

        ResponseEntity<AccountResponse> frozen = post("/api/v1/accounts/" + source.id() + "/freeze", null, AccountResponse.class, adminToken(), null);
        assertThat(frozen.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(frozen.getBody()).isNotNull();
        assertThat(frozen.getBody().status()).isEqualTo(AccountStatus.FROZEN);
        verify(eventPublisher).publish(eq("account-frozen"), eq(source.id()), any());

        ResponseEntity<String> blocked = transfer("idem-transfer-frozen", new TransferRequest(
                source.id(),
                target.id(),
                new BigDecimal("1.00"),
                "USD",
                "Should fail"
        ), customerToken(), String.class);
        assertThat(blocked.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(blocked.getBody()).contains("ACCOUNT_NOT_ACTIVE");
    }

    @Test
    void exposesOpenApiAndHealthEndpoints() {
        ResponseEntity<String> health = get("/actuator/health", String.class, null);
        ResponseEntity<String> openApi = get("/v3/api-docs", String.class, null);

        assertThat(health.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(openApi.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(openApi.getBody()).contains("NeoBankX Account Service");
    }

    private ResponseEntity<AccountResponse> createAccount(String idempotencyKey, CreateAccountRequest request, String token) {
        return post("/api/v1/accounts", request, AccountResponse.class, token, idempotencyKey);
    }

    private ResponseEntity<TransferResponse> transfer(String idempotencyKey, TransferRequest request, String token) {
        return transfer(idempotencyKey, request, token, TransferResponse.class);
    }

    private <T> ResponseEntity<T> transfer(String idempotencyKey, TransferRequest request, String token, Class<T> responseType) {
        return post("/api/v1/accounts/transfers", request, responseType, token, idempotencyKey);
    }

    private ResponseEntity<BalanceResponse> getBalance(UUID accountId, String token) {
        return get("/api/v1/accounts/" + accountId + "/balances", BalanceResponse.class, token);
    }

    private ResponseEntity<LedgerEntryResponse[]> getLedger(UUID accountId, String token) {
        return get("/api/v1/accounts/" + accountId + "/ledger", LedgerEntryResponse[].class, token);
    }

    private <T> ResponseEntity<T> get(String path, Class<T> responseType, String token) {
        HttpHeaders headers = headers(token, null);
        return rest.exchange("http://localhost:" + port + path, HttpMethod.GET, new HttpEntity<>(headers), responseType);
    }

    private <T> ResponseEntity<T> post(String path, Object request, Class<T> responseType, String token, String idempotencyKey) {
        HttpHeaders headers = headers(token, idempotencyKey);
        return rest.postForEntity("http://localhost:" + port + path, new HttpEntity<>(request, headers), responseType);
    }

    private HttpHeaders headers(String token, String idempotencyKey) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        if (idempotencyKey != null) {
            headers.add("Idempotency-Key", idempotencyKey);
        }
        return headers;
    }

    private String customerToken() {
        return token(CUSTOMER_SUBJECT, List.of("CUSTOMER"));
    }

    private String adminToken() {
        return token(ADMIN_SUBJECT, List.of("ADMIN"));
    }

    private String token(String subject, List<String> roles) {
        SecretKeySpec key = new SecretKeySpec(JWT_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("neobankx-auth")
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(900))
                .claim("roles", roles)
                .claim("email", subject + "@example.com")
                .build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    record CreateAccountRequest(String currency, AccountProductType productType, BigDecimal initialDeposit, String ownerSubject) {
    }

    record TransferRequest(UUID fromAccountId, UUID toAccountId, BigDecimal amount, String currency, String memo) {
    }

    record AccountResponse(UUID id, String accountNumber, String ownerSubject, String currency, AccountProductType productType, AccountStatus status, Instant createdAt, long version) {
    }

    record BalanceResponse(UUID accountId, String currency, BigDecimal ledgerBalance, BigDecimal availableBalance, Instant updatedAt, long version) {
    }

    record LedgerEntryResponse(UUID id, UUID accountId, UUID entryGroupId, BigDecimal signedAmount, String currency, String entryType, String memo, Instant createdAt) {
    }

    record TransferResponse(UUID transferId, UUID fromAccountId, UUID toAccountId, BigDecimal amount, String currency, Instant postedAt) {
    }
}

