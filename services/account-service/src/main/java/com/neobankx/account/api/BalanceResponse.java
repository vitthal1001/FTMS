package com.neobankx.account.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BalanceResponse(
        UUID accountId,
        String currency,
        BigDecimal ledgerBalance,
        BigDecimal availableBalance,
        Instant updatedAt,
        long version
) {
}

