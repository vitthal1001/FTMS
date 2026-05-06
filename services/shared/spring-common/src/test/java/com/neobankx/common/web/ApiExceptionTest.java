package com.neobankx.common.web;

import com.neobankx.common.api.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiExceptionTest {
    @Test
    void requiresStableErrorCode() {
        assertThatThrownBy(() -> new ApiException(HttpStatus.BAD_REQUEST, " ", "Invalid request"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("code is required");
    }

    @Test
    void exposesHttpStatusAndCode() {
        ApiException exception = new ApiException(HttpStatus.CONFLICT, "DUPLICATE_REQUEST", "Request already processed");

        assertThat(exception.status()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.code()).isEqualTo("DUPLICATE_REQUEST");
        assertThat(exception).hasMessage("Request already processed");
    }
}

