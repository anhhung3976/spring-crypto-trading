package com.example.cryptotrading.repository;

import com.example.cryptotrading.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRepository extends JpaRepository<TradeEntity, Long> {

    List<TradeEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
