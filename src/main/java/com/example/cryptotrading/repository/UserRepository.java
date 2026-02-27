package com.example.cryptotrading.repository;

import com.example.cryptotrading.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
