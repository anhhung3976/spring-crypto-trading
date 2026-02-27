package com.example.cryptotrading.controller;

import com.example.cryptotrading.dto.PriceResponseDto;
import com.example.cryptotrading.service.PriceService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/prices")
public class PriceController {

    private final PriceService priceService;

    @GetMapping
    public List<PriceResponseDto> getLatestPrices() {
        return priceService.getLatestPrices();
    }
}
