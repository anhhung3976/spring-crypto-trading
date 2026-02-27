package com.example.cryptotrading.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "aggregated_prices")
public class AggregatedPrice {

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

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected AggregatedPrice() {
    }

    public AggregatedPrice(String symbol, BigDecimal bidPrice, BigDecimal askPrice,
                           String bidExchange, String askExchange, LocalDateTime updatedAt) {
        this.symbol = symbol;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
        this.bidExchange = bidExchange;
        this.askExchange = askExchange;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(BigDecimal bidPrice) {
        this.bidPrice = bidPrice;
    }

    public BigDecimal getAskPrice() {
        return askPrice;
    }

    public void setAskPrice(BigDecimal askPrice) {
        this.askPrice = askPrice;
    }

    public String getBidExchange() {
        return bidExchange;
    }

    public void setBidExchange(String bidExchange) {
        this.bidExchange = bidExchange;
    }

    public String getAskExchange() {
        return askExchange;
    }

    public void setAskExchange(String askExchange) {
        this.askExchange = askExchange;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
