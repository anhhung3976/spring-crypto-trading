package com.example.cryptotrading.repository;

import com.example.cryptotrading.entity.TradingPairEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TradingPairRepository extends JpaRepository<TradingPairEntity, Long> {

    List<TradingPairEntity> findByCtlActTrue();

    Optional<TradingPairEntity> findBySymbol(String symbol);
}
