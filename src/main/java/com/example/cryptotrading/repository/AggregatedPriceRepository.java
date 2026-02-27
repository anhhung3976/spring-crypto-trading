package com.example.cryptotrading.repository;

import com.example.cryptotrading.entity.AggregatedPriceEntity;
import com.example.cryptotrading.entity.TradingPairEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AggregatedPriceRepository extends JpaRepository<AggregatedPriceEntity, Long> {

    Optional<AggregatedPriceEntity> findByTradingPair(TradingPairEntity tradingPair);

    Optional<AggregatedPriceEntity> findByTradingPair_Symbol(String symbol);
}
