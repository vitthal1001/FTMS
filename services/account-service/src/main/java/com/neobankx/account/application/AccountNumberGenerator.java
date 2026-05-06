package com.neobankx.account.application;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class AccountNumberGenerator {
    private static final int BODY_LENGTH = 10;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        StringBuilder body = new StringBuilder(BODY_LENGTH);
        for (int index = 0; index < BODY_LENGTH; index++) {
            body.append(secureRandom.nextInt(10));
        }
        return "NBX" + body + checksum(body.toString());
    }

    private int checksum(String body) {
        int sum = 0;
        for (int index = 0; index < body.length(); index++) {
            int digit = Character.digit(body.charAt(index), 10);
            sum += index % 2 == 0 ? digit * 3 : digit;
        }
        return (10 - (sum % 10)) % 10;
    }
}

