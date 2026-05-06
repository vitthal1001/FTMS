package com.neobankx.account.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neobankx.account.infrastructure.persistence.IdempotencyRecordEntity;
import com.neobankx.account.infrastructure.persistence.IdempotencyRecordRepository;
import com.neobankx.common.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class IdempotencyService {
    private static final Duration RECORD_TTL = Duration.ofHours(24);

    private final IdempotencyRecordRepository records;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public IdempotencyService(IdempotencyRecordRepository records, ObjectMapper objectMapper, Clock clock) {
        this.records = records;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public <T> T execute(String idempotencyKey, String operation, Object request, Class<T> responseType, Supplier<T> operationSupplier) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "IDEMPOTENCY_KEY_REQUIRED", "Idempotency-Key header is required");
        }
        if (idempotencyKey.length() > 120) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "IDEMPOTENCY_KEY_TOO_LONG", "Idempotency-Key header is too long");
        }
        String requestHash = sha256(operation + ":" + writeJson(request));
        Optional<IdempotencyRecordEntity> existing = records.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            IdempotencyRecordEntity record = existing.get();
            if (!record.getOperation().equals(operation) || !record.getRequestHash().equals(requestHash)) {
                throw new ApiException(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_REUSED", "Idempotency key was reused with a different request");
            }
            return readJson(record.getResponseBody(), responseType);
        }

        T response = operationSupplier.get();
        records.save(new IdempotencyRecordEntity(
                idempotencyKey,
                operation,
                requestHash,
                200,
                writeJson(response),
                clock.instant(),
                clock.instant().plus(RECORD_TTL)
        ));
        return response;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize idempotency value", exception);
        }
    }

    private <T> T readJson(String value, Class<T> responseType) {
        try {
            return objectMapper.readValue(value, responseType);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to deserialize idempotency value", exception);
        }
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
