package com.neobankx.account.api;

import com.neobankx.account.domain.AccountProductType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @Pattern(regexp = "^[A-Z]{3}$") String currency,
        AccountProductType productType,
        @DecimalMin(value = "0.00") BigDecimal initialDeposit,
        @Size(max = 120) String ownerSubject
) {
}

