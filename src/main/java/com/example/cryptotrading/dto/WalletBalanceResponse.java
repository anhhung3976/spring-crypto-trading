package com.example.cryptotrading.dto;

import java.math.BigDecimal;

public record WalletBalanceResponse(
        String currency,
        BigDecimal balance
) {
}
