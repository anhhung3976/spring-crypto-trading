package com.example.cryptotrading.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.example.cryptotrading.client.BinanceClient;
import com.example.cryptotrading.client.BinanceClient.BookTicker;
import com.example.cryptotrading.client.HuobiClient;
import com.example.cryptotrading.dto.PriceResponseDto;
import com.example.cryptotrading.entity.AggregatedPriceEntity;
import com.example.cryptotrading.repository.AggregatedPriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PriceService {

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

        for (String symbol : SUPPORTED_SYMBOLS) {
            BookTicker binance = binanceTickers.get(symbol);
            BookTicker huobi = huobiTickers.get(symbol);

            if (binance == null && huobi == null) {
                log.warn("No data available for {} from either exchange", symbol);
                continue;
            }

            AggregatedPriceEntity price = priceRepository.findBySymbol(symbol)
                    .orElse(new AggregatedPriceEntity(symbol, null, null, null, null));

            computeBestBid(price, binance, huobi);
            computeBestAsk(price, binance, huobi);

            priceRepository.save(price);
            log.info("Aggregated {} - bid: {} ({}), ask: {} ({})",
                    symbol, price.getBidPrice(), price.getBidExchange(),
                    price.getAskPrice(), price.getAskExchange());
        }
    }

    private void computeBestBid(AggregatedPriceEntity price, BookTicker binance, BookTicker huobi) {
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

    private void computeBestAsk(AggregatedPriceEntity price, BookTicker binance, BookTicker huobi) {
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
    public List<PriceResponseDto> getLatestPrices() {
        return priceRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<AggregatedPriceEntity> getLatestPrice(String symbol) {
        return priceRepository.findBySymbol(symbol.toUpperCase());
    }

    private PriceResponseDto toResponse(AggregatedPriceEntity entity) {
        return new PriceResponseDto(
                entity.getSymbol(),
                entity.getBidPrice(),
                entity.getAskPrice(),
                entity.getBidExchange(),
                entity.getAskExchange(),
                entity.getUpdatedAt()
        );
    }
}
