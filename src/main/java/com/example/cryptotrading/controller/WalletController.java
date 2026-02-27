package com.example.cryptotrading.controller;

import com.example.cryptotrading.dto.WalletBalanceResponse;
import com.example.cryptotrading.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private static final Long DEFAULT_USER_ID = 1L;

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public ResponseEntity<List<WalletBalanceResponse>> getBalances() {
        List<WalletBalanceResponse> balances = walletService.getBalances(DEFAULT_USER_ID);
        return ResponseEntity.ok(balances);
    }
}
