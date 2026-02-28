package com.example.cryptotrading.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderSideCodeEnum {

    BUY,
    SELL;

    @JsonCreator
    public static OrderSideCodeEnum fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid side: " + value + ". Must be BUY or SELL", e);
        }
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
