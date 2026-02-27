package com.example.cryptotrading.dto;

import java.math.BigDecimal;

public record WalletBalanceResponseDto(
        String currency,
        BigDecimal balance
) {
}

