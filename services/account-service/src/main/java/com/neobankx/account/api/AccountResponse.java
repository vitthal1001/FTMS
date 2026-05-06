package com.neobankx.account.api;

import com.neobankx.account.domain.AccountProductType;
import com.neobankx.account.domain.AccountStatus;

import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String accountNumber,
        String ownerSubject,
        String currency,
        AccountProductType productType,
        AccountStatus status,
        Instant createdAt,
        long version
) {
}

