package com.neobankx.auth.application;

import com.neobankx.common.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class PasswordPolicy {
    private static final int MIN_LENGTH = 12;
    private static final Pattern UPPER = Pattern.compile("[A-Z]");
    private static final Pattern LOWER = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SYMBOL = Pattern.compile("[^A-Za-z0-9]");
    private static final List<String> BLOCKED_FRAGMENTS = List.of("password", "neobankx", "qwerty", "letmein");

    public void validate(String password, String email) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw invalidPassword("Password must be at least 12 characters");
        }
        if (!UPPER.matcher(password).find() || !LOWER.matcher(password).find()
                || !DIGIT.matcher(password).find() || !SYMBOL.matcher(password).find()) {
            throw invalidPassword("Password must include uppercase, lowercase, number, and symbol characters");
        }
        String normalizedPassword = password.toLowerCase();
        for (String blocked : BLOCKED_FRAGMENTS) {
            if (normalizedPassword.contains(blocked)) {
                throw invalidPassword("Password contains a blocked phrase");
            }
        }
        if (email != null) {
            String localPart = email.split("@")[0].toLowerCase();
            if (!localPart.isBlank() && normalizedPassword.contains(localPart)) {
                throw invalidPassword("Password must not contain email identifiers");
            }
        }
    }

    private ApiException invalidPassword(String detail) {
        return new ApiException(HttpStatus.BAD_REQUEST, "WEAK_PASSWORD", detail);
    }
}

