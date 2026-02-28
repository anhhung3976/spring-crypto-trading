package com.example.cryptotrading.entity;

import static com.example.cryptotrading.entity.AggregatedPriceEntity.TABLE_NAME;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = TABLE_NAME)
@Getter
@Setter
@NoArgsConstructor
public class AggregatedPriceEntity extends BaseEntity {

    public static final String TABLE_NAME = "crypto_aggregated_price";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 30, scale = 8)
    private BigDecimal bidPrice;

    @Column(nullable = false, precision = 30, scale = 8)
    private BigDecimal askPrice;

    @Column(length = 20)
    private String bidExchange;

    @Column(length = 20)
    private String askExchange;

    @Column(name = "last_checked_at")
    private LocalDateTime lastCheckedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trading_pair_id", nullable = false, unique = true)
    private TradingPairEntity tradingPair;

    public AggregatedPriceEntity(TradingPairEntity tradingPair, BigDecimal bidPrice, BigDecimal askPrice,
                                 String bidExchange, String askExchange) {
        this.tradingPair = tradingPair;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
        this.bidExchange = bidExchange;
        this.askExchange = askExchange;
    }

}
