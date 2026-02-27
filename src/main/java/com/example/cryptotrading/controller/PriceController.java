package com.example.cryptotrading.controller;

import com.example.cryptotrading.dto.PriceResponse;
import com.example.cryptotrading.service.PriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/prices")
public class PriceController {

    private final PriceService priceService;

    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping
    public ResponseEntity<List<PriceResponse>> getLatestPrices() {
        List<PriceResponse> prices = priceService.getLatestPrices();
        return ResponseEntity.ok(prices);
    }
}
