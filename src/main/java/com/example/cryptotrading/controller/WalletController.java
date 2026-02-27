package com.example.cryptotrading.controller;

import com.example.cryptotrading.dto.WalletBalanceResponseDto;
import com.example.cryptotrading.service.WalletService;
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
    public List<WalletBalanceResponseDto> getBalances() {
        return walletService.getBalances(DEFAULT_USER_ID);
    }
}
