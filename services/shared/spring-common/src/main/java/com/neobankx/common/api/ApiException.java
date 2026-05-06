package com.neobankx.common.api;

import org.springframework.http.HttpStatus;

import java.util.Objects;

public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = Objects.requireNonNull(status, "status is required");
        this.code = requireNonBlank(code, "code");
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value;
    }
}

