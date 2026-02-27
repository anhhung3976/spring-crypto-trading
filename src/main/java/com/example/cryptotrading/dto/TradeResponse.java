package com.example.cryptotrading.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeResponse(
        Long tradeId,
        String symbol,
        String side,
        BigDecimal price,
        BigDecimal quantity,
        BigDecimal cost,
        LocalDateTime createdAt
) {
}
