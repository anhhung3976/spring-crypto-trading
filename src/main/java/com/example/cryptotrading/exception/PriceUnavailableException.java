package com.example.cryptotrading.exception;

public class PriceUnavailableException extends RuntimeException {

    public PriceUnavailableException(String message) {
        super(message);
    }
}
