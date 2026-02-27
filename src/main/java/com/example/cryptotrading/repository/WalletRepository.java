package com.example.cryptotrading.repository;

import com.example.cryptotrading.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<WalletEntity, Long> {

    List<WalletEntity> findByUserId(Long userId);

    Optional<WalletEntity> findByUserIdAndCurrencyId(Long userId, Long currencyId);

    Optional<WalletEntity> findByUserIdAndCurrency_Code(Long userId, String currencyCode);
}
