package com.example.cryptotrading.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceResponse(
        String symbol,
        BigDecimal bidPrice,
        BigDecimal askPrice,
        String bidExchange,
        String askExchange,
        LocalDateTime updatedAt
) {
}
