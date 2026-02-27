package com.example.cryptotrading.service;

import com.example.cryptotrading.dto.WalletBalanceResponseDto;
import com.example.cryptotrading.entity.WalletEntity;
import com.example.cryptotrading.exception.InsufficientBalanceException;
import com.example.cryptotrading.repository.WalletRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    @Transactional(readOnly = true)
    public List<WalletBalanceResponseDto> getBalances(Long userId) {
        return walletRepository.findByUserId(userId).stream()
                .map(w -> new WalletBalanceResponseDto(w.getCurrency().getCode(), w.getBalance()))
                .toList();
    }

    @Transactional
    public void debit(Long userId, String currencyCode, BigDecimal amount) {
        WalletEntity wallet = walletRepository.findByUserIdAndCurrencyCode(userId, currencyCode)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for currency: " + currencyCode));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient " + currencyCode + " balance. Available: " + wallet.getBalance() + ", required: " + amount);
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
    }

    @Transactional
    public void credit(Long userId, String currencyCode, BigDecimal amount) {
        WalletEntity wallet = walletRepository.findByUserIdAndCurrencyCode(userId, currencyCode)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for currency: " + currencyCode));

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
    }
}
