package com.neobankx.account.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_records")
public class IdempotencyRecordEntity {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 160)
    private String idempotencyKey;

    @Column(nullable = false, length = 80)
    private String operation;

    @Column(nullable = false, length = 64)
    private String requestHash;

    @Column(nullable = false)
    private int responseStatus;

    @Column(nullable = false, columnDefinition = "text")
    private String responseBody;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Version
    private long version;

    protected IdempotencyRecordEntity() {
    }

    public IdempotencyRecordEntity(String idempotencyKey, String operation, String requestHash, int responseStatus, String responseBody, Instant createdAt, Instant expiresAt) {
        this.id = UUID.randomUUID();
        this.idempotencyKey = idempotencyKey;
        this.operation = operation;
        this.requestHash = requestHash;
        this.responseStatus = responseStatus;
        this.responseBody = responseBody;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getOperation() {
        return operation;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }
}

