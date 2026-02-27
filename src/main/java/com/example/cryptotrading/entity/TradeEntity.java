package com.example.cryptotrading.entity;

import static com.example.cryptotrading.entity.TradeEntity.TABLE_NAME;

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
public class TradeEntity extends BaseEntity {

    public static final String TABLE_NAME = "crypto_trade";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 30, scale = 8)
    private BigDecimal price;

    @Column(nullable = false, precision = 30, scale = 8)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 30, scale = 8)
    private BigDecimal cost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trading_pair_id", nullable = false)
    private TradingPairEntity tradingPair;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_side_id", nullable = false)
    private OrderSideEntity orderSide;

    public TradeEntity(Long userId, TradingPairEntity tradingPair, OrderSideEntity orderSide,
                       BigDecimal price, BigDecimal quantity, BigDecimal cost) {
        this.userId = userId;
        this.tradingPair = tradingPair;
        this.orderSide = orderSide;
        this.price = price;
        this.quantity = quantity;
        this.cost = cost;
    }

    public LocalDateTime getCreatedAt() {
        return getCtlCreTs();
    }
}
