package com.example.cryptotrading.dto;

import com.example.cryptotrading.domain.OrderSideCodeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TradeRequestDto(
        @NotBlank(message = "Symbol is required")
        String symbol,

        @NotNull(message = "Side is required (BUY or SELL)")
        OrderSideCodeEnum side,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        BigDecimal quantity
) {
}

