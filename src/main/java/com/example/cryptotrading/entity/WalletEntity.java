package com.example.cryptotrading.entity;

import static com.example.cryptotrading.entity.WalletEntity.TABLE_NAME;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = TABLE_NAME, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"userId", "currency"})
})
@Getter
@Setter
@NoArgsConstructor
public class WalletEntity extends BaseEntity {

    public static final String TABLE_NAME = "crypto_wallet";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(nullable = false, precision = 30, scale = 8)
    private BigDecimal balance;

}
