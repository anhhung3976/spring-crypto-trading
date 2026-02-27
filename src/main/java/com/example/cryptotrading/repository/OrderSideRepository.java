package com.example.cryptotrading.repository;

import com.example.cryptotrading.entity.OrderSideEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderSideRepository extends JpaRepository<OrderSideEntity, Long> {

    Optional<OrderSideEntity> findByCode(String code);
}
