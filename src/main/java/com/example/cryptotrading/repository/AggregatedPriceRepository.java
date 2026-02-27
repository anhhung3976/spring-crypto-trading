package com.example.cryptotrading.repository;

import com.example.cryptotrading.entity.AggregatedPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AggregatedPriceRepository extends JpaRepository<AggregatedPriceEntity, Long> {

    Optional<AggregatedPriceEntity> findBySymbol(String symbol);
}
