package com.example.cryptotrading.controller;

import com.example.cryptotrading.dto.TradeRequestDto;
import com.example.cryptotrading.dto.TradeResponseDto;
import com.example.cryptotrading.service.TradeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private static final Long DEFAULT_USER_ID = 1L;

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping
    public ResponseEntity<TradeResponseDto> executeTrade(@Valid @RequestBody TradeRequestDto request) {
        TradeResponseDto response = tradeService.executeTrade(DEFAULT_USER_ID, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<TradeResponseDto> getTradeHistory() {
        return tradeService.getTradeHistory(DEFAULT_USER_ID);
    }
}
