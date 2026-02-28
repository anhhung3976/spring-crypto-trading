package com.example.cryptotrading.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class AmountFormatUtil {

    private static final int SCALE = 8;
    private static final String ZERO_FORMATTED = "0.00000000";

    public static String format(BigDecimal amount) {
        if (amount == null) {
            return ZERO_FORMATTED;
        }
        return amount.setScale(SCALE, RoundingMode.DOWN).toPlainString();
    }
}

