package com.neobankx.account.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account_balance_snapshots")
public class BalanceSnapshotEntity {
    @Id
    private UUID accountId;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal ledgerBalance;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal availableBalance;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected BalanceSnapshotEntity() {
    }

    public BalanceSnapshotEntity(UUID accountId, String currency, BigDecimal openingBalance, Instant now) {
        this.accountId = accountId;
        this.currency = currency;
        this.ledgerBalance = openingBalance;
        this.availableBalance = openingBalance;
        this.updatedAt = now;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getLedgerBalance() {
        return ledgerBalance;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }

    public void apply(BigDecimal signedAmount, Instant now) {
        this.ledgerBalance = ledgerBalance.add(signedAmount);
        this.availableBalance = availableBalance.add(signedAmount);
        this.updatedAt = now;
    }
}

