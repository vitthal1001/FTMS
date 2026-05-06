package com.neobankx.account;

import com.neobankx.account.domain.Money;
import com.neobankx.common.api.ApiException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {
    @Test
    void normalizesValidMoney() {
        Money money = new Money(new BigDecimal("12.30"), "usd");

        assertThat(money.amount()).isEqualByComparingTo("12.30");
        assertThat(money.currency()).isEqualTo("USD");
    }

    @Test
    void rejectsPrecisionBeyondMinorUnits() {
        assertThatThrownBy(() -> new Money(new BigDecimal("12.345"), "USD"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Amount supports at most two decimal places");
    }
}

