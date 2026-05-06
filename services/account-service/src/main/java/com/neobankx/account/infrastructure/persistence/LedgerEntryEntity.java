package com.neobankx.account.infrastructure.persistence;

import com.neobankx.account.domain.LedgerEntryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntryEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false)
    private UUID entryGroupId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal signedAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private LedgerEntryType entryType;

    @Column(length = 240)
    private String memo;

    @Column(nullable = false)
    private Instant createdAt;

    protected LedgerEntryEntity() {
    }

    public LedgerEntryEntity(UUID accountId, UUID entryGroupId, BigDecimal signedAmount, String currency, LedgerEntryType entryType, String memo, Instant createdAt) {
        this.id = UUID.randomUUID();
        this.accountId = accountId;
        this.entryGroupId = entryGroupId;
        this.signedAmount = signedAmount;
        this.currency = currency;
        this.entryType = entryType;
        this.memo = memo;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getEntryGroupId() {
        return entryGroupId;
    }

    public BigDecimal getSignedAmount() {
        return signedAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public LedgerEntryType getEntryType() {
        return entryType;
    }

    public String getMemo() {
        return memo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

