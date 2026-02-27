package com.example.cryptotrading.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.cryptotrading.client.BinanceClient;
import com.example.cryptotrading.client.BinanceClient.BookTicker;
import com.example.cryptotrading.client.HuobiClient;
import com.example.cryptotrading.dto.PriceResponseDto;
import com.example.cryptotrading.entity.AggregatedPriceEntity;
import com.example.cryptotrading.entity.TradingPairEntity;
import com.example.cryptotrading.repository.AggregatedPriceRepository;
import com.example.cryptotrading.repository.TradingPairRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class PriceService {

    public static final String BINANCE = "BINANCE";
    public static final String HUOBI = "HUOBI";

    private final BinanceClient binanceClient;
    private final HuobiClient huobiClient;
    private final AggregatedPriceRepository priceRepository;
    private final TradingPairRepository tradingPairRepository;

    @Transactional
    public void aggregatePrices() {
        List<TradingPairEntity> activePairs = tradingPairRepository.findByCtlActTrue();
        if (activePairs.isEmpty()) {
            log.warn("No active trading pairs configured, skipping aggregation");
            return;
        }

        Set<String> symbols = activePairs.stream()
                .map(TradingPairEntity::getSymbol)
                .collect(Collectors.toSet());

        Map<String, BookTicker> binanceTickers = binanceClient.getBookTickers(symbols);
        Map<String, BookTicker> huobiTickers = huobiClient.getBookTickers(symbols);

        if (binanceTickers.isEmpty() && huobiTickers.isEmpty()) {
            log.warn("Both exchanges returned empty data, skipping aggregation");
            return;
        }

        for (TradingPairEntity pair : activePairs) {
            String symbol = pair.getSymbol();
            BookTicker binance = binanceTickers.get(symbol);
            BookTicker huobi = huobiTickers.get(symbol);

            if (binance == null && huobi == null) {
                log.warn("No data available for {} from either exchange", symbol);
                continue;
            }

            AggregatedPriceEntity price = priceRepository.findByTradingPairId(pair.getId())
                    .orElseGet(() -> new AggregatedPriceEntity(pair.getId(), null, null, null, null));

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
                price.setBidExchange(BINANCE);
            } else {
                price.setBidPrice(huobi.bidPrice());
                price.setBidExchange(HUOBI);
            }
        } else if (binance != null) {
            price.setBidPrice(binance.bidPrice());
            price.setBidExchange(BINANCE);
        } else {
            price.setBidPrice(huobi.bidPrice());
            price.setBidExchange(HUOBI);
        }
    }

    private void computeBestAsk(AggregatedPriceEntity price, BookTicker binance, BookTicker huobi) {
        if (binance != null && huobi != null) {
            if (binance.askPrice().compareTo(huobi.askPrice()) <= 0) {
                price.setAskPrice(binance.askPrice());
                price.setAskExchange(BINANCE);
            } else {
                price.setAskPrice(huobi.askPrice());
                price.setAskExchange(HUOBI);
            }
        } else if (binance != null) {
            price.setAskPrice(binance.askPrice());
            price.setAskExchange(BINANCE);
        } else {
            price.setAskPrice(huobi.askPrice());
            price.setAskExchange(HUOBI);
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
        return priceRepository.findByTradingPair_Symbol(symbol.toUpperCase());
    }

    private PriceResponseDto toResponse(AggregatedPriceEntity entity) {
        return new PriceResponseDto(
                entity.getTradingPair().getSymbol(),
                entity.getBidPrice(),
                entity.getAskPrice(),
                entity.getBidExchange(),
                entity.getAskExchange(),
                entity.getUpdatedAt()
        );
    }
}
