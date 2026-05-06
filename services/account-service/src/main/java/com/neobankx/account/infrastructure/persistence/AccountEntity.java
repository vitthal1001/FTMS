package com.neobankx.account.infrastructure.persistence;

import com.neobankx.account.domain.AccountProductType;
import com.neobankx.account.domain.AccountStatus;
import com.neobankx.common.api.ApiException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class AccountEntity {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 32)
    private String accountNumber;

    @Column(nullable = false, length = 120)
    private String ownerSubject;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AccountProductType productType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AccountStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected AccountEntity() {
    }

    public AccountEntity(UUID id, String accountNumber, String ownerSubject, String currency, AccountProductType productType, Instant now) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.ownerSubject = ownerSubject;
        this.currency = currency;
        this.productType = productType;
        this.status = AccountStatus.ACTIVE;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerSubject() {
        return ownerSubject;
    }

    public String getCurrency() {
        return currency;
    }

    public AccountProductType getProductType() {
        return productType;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public long getVersion() {
        return version;
    }

    public boolean isOwnedBy(String subject) {
        return ownerSubject.equals(subject);
    }

    public void requireActive() {
        if (status != AccountStatus.ACTIVE) {
            throw new ApiException(HttpStatus.CONFLICT, "ACCOUNT_NOT_ACTIVE", "Account is not active");
        }
    }

    public void freeze(Instant now) {
        if (productType == AccountProductType.SYSTEM) {
            throw new ApiException(HttpStatus.CONFLICT, "SYSTEM_ACCOUNT_IMMUTABLE", "System account status cannot be changed");
        }
        status = AccountStatus.FROZEN;
        updatedAt = now;
    }

    public void activate(Instant now) {
        if (productType == AccountProductType.SYSTEM) {
            throw new ApiException(HttpStatus.CONFLICT, "SYSTEM_ACCOUNT_IMMUTABLE", "System account status cannot be changed");
        }
        status = AccountStatus.ACTIVE;
        updatedAt = now;
    }
}

