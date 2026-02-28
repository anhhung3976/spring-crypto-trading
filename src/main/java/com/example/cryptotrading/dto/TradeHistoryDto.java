package com.example.cryptotrading.dto;

import com.example.cryptotrading.domain.OrderSideCodeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeHistoryDto(
        Long tradeId,
        String symbol,
        OrderSideCodeEnum side,
        BigDecimal price,
        BigDecimal quantity,
        BigDecimal cost,
        LocalDateTime createdAt
) {
}

