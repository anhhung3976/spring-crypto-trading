package com.example.cryptotrading.repository;

import com.example.cryptotrading.entity.TradeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<TradeEntity, Long> {

    @EntityGraph(value = "Trade.withRelations", type = EntityGraph.EntityGraphType.LOAD)
    Page<TradeEntity> findByUserIdOrderByCtlCreTsDesc(Long userId, Pageable pageable);
}
