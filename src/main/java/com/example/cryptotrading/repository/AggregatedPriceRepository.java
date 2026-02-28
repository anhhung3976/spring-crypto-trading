package com.example.cryptotrading.repository;

import com.example.cryptotrading.entity.AggregatedPriceEntity;
import com.example.cryptotrading.entity.TradingPairEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AggregatedPriceRepository extends JpaRepository<AggregatedPriceEntity, Long> {

    Optional<AggregatedPriceEntity> findByTradingPair(TradingPairEntity tradingPair);

    Optional<AggregatedPriceEntity> findByTradingPairSymbol(String symbol);

    @Modifying
    @Query("UPDATE AggregatedPriceEntity p SET p.lastCheckedAt = :ts WHERE p.tradingPair = :pair")
    int touchLastChecked(@Param("pair") TradingPairEntity pair, @Param("ts") LocalDateTime ts);
}
