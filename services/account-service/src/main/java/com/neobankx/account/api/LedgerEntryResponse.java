package com.neobankx.account.api;

import com.neobankx.account.domain.LedgerEntryType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LedgerEntryResponse(
        UUID id,
        UUID accountId,
        UUID entryGroupId,
        BigDecimal signedAmount,
        String currency,
        LedgerEntryType entryType,
        String memo,
        Instant createdAt
) {
}

