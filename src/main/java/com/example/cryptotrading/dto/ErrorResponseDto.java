package com.example.cryptotrading.dto;

import java.time.LocalDateTime;

public record ErrorResponseDto(
        int status,
        String message,
        LocalDateTime timestamp
) {
    public static ErrorResponseDto of(int status, String message) {
        return new ErrorResponseDto(status, message, LocalDateTime.now());
    }
}

