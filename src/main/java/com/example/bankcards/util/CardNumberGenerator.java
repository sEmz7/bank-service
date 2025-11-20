package com.example.bankcards.util;

import java.util.Random;

public class CardNumberGenerator {
    private static final String BIN = "773377";

    private static final Random random = new Random();

    public static String generateCardNumber() {
        StringBuilder sb = new StringBuilder(BIN);

        for (int i = 0; i < 10; i++) {
            sb.append(random.nextInt(10));
        }

        return sb.toString();
    }
}
