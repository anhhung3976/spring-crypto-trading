package com.example.cryptotrading.service;

import com.example.cryptotrading.dto.WalletBalanceResponseDto;
import com.example.cryptotrading.entity.WalletEntity;
import com.example.cryptotrading.exception.InsufficientBalanceException;
import com.example.cryptotrading.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional(readOnly = true)
    public List<WalletBalanceResponseDto> getBalances(Long userId) {
        return walletRepository.findByUserId(userId).stream()
                .map(w -> new WalletBalanceResponseDto(w.getCurrency(), w.getBalance()))
                .toList();
    }

    @Transactional
    public void debit(Long userId, String currency, BigDecimal amount) {
        WalletEntity wallet = walletRepository.findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for currency: " + currency));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient " + currency + " balance. Available: " + wallet.getBalance() + ", required: " + amount);
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
    }

    @Transactional
    public void credit(Long userId, String currency, BigDecimal amount) {
        WalletEntity wallet = walletRepository.findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for currency: " + currency));

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
    }
}
