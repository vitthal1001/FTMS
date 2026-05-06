package com.neobankx.auth;

import com.neobankx.auth.application.PasswordPolicy;
import com.neobankx.common.api.ApiException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordPolicyTest {
    private final PasswordPolicy passwordPolicy = new PasswordPolicy();

    @Test
    void acceptsStrongPassword() {
        assertThatCode(() -> passwordPolicy.validate("Stronger!Passphrase2026", "customer@example.com"))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsEmailDerivedPassword() {
        assertThatThrownBy(() -> passwordPolicy.validate("Customer!2026X", "customer@example.com"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Password must not contain email identifiers");
    }
}

