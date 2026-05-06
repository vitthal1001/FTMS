package com.neobankx.account.domain;

import com.neobankx.common.api.ApiException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public record Money(BigDecimal amount, String currency) {
    public Money {
        if (amount == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT", "Amount is required");
        }
        if (currency == null || currency.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CURRENCY", "Currency is required");
        }
        currency = currency.toUpperCase();
        Currency.getInstance(currency);
        if (amount.scale() > 2) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT_SCALE", "Amount supports at most two decimal places");
        }
        amount = amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO.setScale(2), currency);
    }

    public void requireNonNegative() {
        if (amount.signum() < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "NEGATIVE_AMOUNT", "Amount cannot be negative");
        }
    }

    public void requirePositive() {
        if (amount.signum() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "NON_POSITIVE_AMOUNT", "Amount must be greater than zero");
        }
    }
}
