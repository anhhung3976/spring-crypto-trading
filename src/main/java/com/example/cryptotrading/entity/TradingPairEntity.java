package com.example.cryptotrading.entity;

import static com.example.cryptotrading.entity.TradingPairEntity.TABLE_NAME;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = TABLE_NAME)
@Getter
@Setter
@NoArgsConstructor
public class TradingPairEntity extends BaseEntity {

    public static final String TABLE_NAME = "crypto_trading_pair";

    @Id
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String symbol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_currency_id", nullable = false)
    private CurrencyEntity baseCurrency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_currency_id", nullable = false)
    private CurrencyEntity quoteCurrency;

    public TradingPairEntity(Long id, String symbol) {
        this.id = id;
        this.symbol = symbol;
    }
}
