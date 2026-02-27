package com.example.cryptotrading.entity;

import static com.example.cryptotrading.entity.AggregatedPriceEntity.TABLE_NAME;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Column(nullable = false, unique = true, length = 10)
    private String symbol;

    @Column(nullable = false, precision = 30, scale = 8)
    private BigDecimal bidPrice;

    @Column(nullable = false, precision = 30, scale = 8)
    private BigDecimal askPrice;

    @Column(length = 20)
    private String bidExchange;

    @Column(length = 20)
    private String askExchange;

    public AggregatedPriceEntity(String symbol, BigDecimal bidPrice, BigDecimal askPrice,
                                 String bidExchange, String askExchange) {
        this.symbol = symbol;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
        this.bidExchange = bidExchange;
        this.askExchange = askExchange;
    }

    public LocalDateTime getUpdatedAt() {
        return getCtlModTs() != null ? getCtlModTs() : getCtlCreTs();
    }
}
