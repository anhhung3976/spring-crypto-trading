package com.example.cryptotrading.service;

import com.example.cryptotrading.client.BinanceClient;
import com.example.cryptotrading.client.BinanceClient.BookTicker;
import com.example.cryptotrading.client.HuobiClient;
import com.example.cryptotrading.dto.PriceResponse;
import com.example.cryptotrading.entity.AggregatedPrice;
import com.example.cryptotrading.repository.AggregatedPriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class PriceService {

    private static final Logger log = LoggerFactory.getLogger(PriceService.class);
    private static final Set<String> SUPPORTED_SYMBOLS = Set.of("BTCUSDT", "ETHUSDT");

    private final BinanceClient binanceClient;
    private final HuobiClient huobiClient;
    private final AggregatedPriceRepository priceRepository;

    public PriceService(BinanceClient binanceClient, HuobiClient huobiClient,
                        AggregatedPriceRepository priceRepository) {
        this.binanceClient = binanceClient;
        this.huobiClient = huobiClient;
        this.priceRepository = priceRepository;
    }

    @Transactional
    public void aggregatePrices() {
        Map<String, BookTicker> binanceTickers = binanceClient.getBookTickers();
        Map<String, BookTicker> huobiTickers = huobiClient.getBookTickers();

        if (binanceTickers.isEmpty() && huobiTickers.isEmpty()) {
            log.warn("Both exchanges returned empty data, skipping aggregation");
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        for (String symbol : SUPPORTED_SYMBOLS) {
            BookTicker binance = binanceTickers.get(symbol);
            BookTicker huobi = huobiTickers.get(symbol);

            if (binance == null && huobi == null) {
                log.warn("No data available for {} from either exchange", symbol);
                continue;
            }

            AggregatedPrice price = priceRepository.findBySymbol(symbol)
                    .orElse(new AggregatedPrice(symbol, null, null, null, null, now));

            computeBestBid(price, binance, huobi);
            computeBestAsk(price, binance, huobi);
            price.setUpdatedAt(now);

            priceRepository.save(price);
            log.info("Aggregated {} - bid: {} ({}), ask: {} ({})",
                    symbol, price.getBidPrice(), price.getBidExchange(),
                    price.getAskPrice(), price.getAskExchange());
        }
    }

    private void computeBestBid(AggregatedPrice price, BookTicker binance, BookTicker huobi) {
        if (binance != null && huobi != null) {
            if (binance.bidPrice().compareTo(huobi.bidPrice()) >= 0) {
                price.setBidPrice(binance.bidPrice());
                price.setBidExchange("BINANCE");
            } else {
                price.setBidPrice(huobi.bidPrice());
                price.setBidExchange("HUOBI");
            }
        } else if (binance != null) {
            price.setBidPrice(binance.bidPrice());
            price.setBidExchange("BINANCE");
        } else {
            price.setBidPrice(huobi.bidPrice());
            price.setBidExchange("HUOBI");
        }
    }

    private void computeBestAsk(AggregatedPrice price, BookTicker binance, BookTicker huobi) {
        if (binance != null && huobi != null) {
            if (binance.askPrice().compareTo(huobi.askPrice()) <= 0) {
                price.setAskPrice(binance.askPrice());
                price.setAskExchange("BINANCE");
            } else {
                price.setAskPrice(huobi.askPrice());
                price.setAskExchange("HUOBI");
            }
        } else if (binance != null) {
            price.setAskPrice(binance.askPrice());
            price.setAskExchange("BINANCE");
        } else {
            price.setAskPrice(huobi.askPrice());
            price.setAskExchange("HUOBI");
        }
    }

    @Transactional(readOnly = true)
    public List<PriceResponse> getLatestPrices() {
        return priceRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<AggregatedPrice> getLatestPrice(String symbol) {
        return priceRepository.findBySymbol(symbol.toUpperCase());
    }

    private PriceResponse toResponse(AggregatedPrice entity) {
        return new PriceResponse(
                entity.getSymbol(),
                entity.getBidPrice(),
                entity.getAskPrice(),
                entity.getBidExchange(),
                entity.getAskExchange(),
                entity.getUpdatedAt()
        );
    }
}
