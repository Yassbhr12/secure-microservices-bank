package com.securebank.account.application;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class AccountNumberGenerator {

    private static final String PREFIX = "SB";
    private static final int RANDOM_DIGITS = 26;

    private final SecureRandom secureRandom =
        new SecureRandom();

    public String generate() {
        StringBuilder accountNumber =
            new StringBuilder(PREFIX);

        for (int index = 0;
             index < RANDOM_DIGITS;
             index++) {

            accountNumber.append(
                secureRandom.nextInt(10)
            );
        }

        return accountNumber.toString();
    }
}
