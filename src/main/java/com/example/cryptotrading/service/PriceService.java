package com.example.cryptotrading.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
                log.warn("No data available for pairId {}, symbol {} from either exchange", pair.getId(), symbol);
                continue;
            }

            AggregatedPriceEntity price = priceRepository.findByTradingPair(pair)
                    .orElseGet(() -> new AggregatedPriceEntity(pair, null, null, null, null));

            BigDecimal originalBid = price.getBidPrice();
            BigDecimal originalAsk = price.getAskPrice();
            String originalBidExchange = price.getBidExchange();
            String originalAskExchange = price.getAskExchange();

            computeBestBid(price, binance, huobi);
            computeBestAsk(price, binance, huobi);

            if (!isChanged(originalBid, price, originalAsk, originalBidExchange, originalAskExchange)) {
                log.info("Aggregated price for pairId {}, symbol {} unchanged, skipping save", pair.getId(), symbol);
                continue;
            }

            priceRepository.save(price);
            log.info("Aggregated pairId {}, symbol {} - bid: {} ({}), ask: {} ({})",
                    pair.getId(), symbol, price.getBidPrice(), price.getBidExchange(),
                    price.getAskPrice(), price.getAskExchange());
        }
    }

    private static boolean isChanged(BigDecimal originalBid, AggregatedPriceEntity price, BigDecimal originalAsk,
            String originalBidExchange, String originalAskExchange) {
        return !Objects.equals(originalBid, price.getBidPrice())
                || !Objects.equals(originalAsk, price.getAskPrice())
                || !Objects.equals(originalBidExchange, price.getBidExchange())
                || !Objects.equals(originalAskExchange, price.getAskExchange());
    }

    private void computeBestBid(AggregatedPriceEntity price, BookTicker binance, BookTicker huobi) {
        BigDecimal bestBid = null;
        String bestExchange = null;

        if (binance != null) {
            bestBid = binance.bidPrice();
            bestExchange = BINANCE;
        }
        if (huobi != null) {
            if (bestBid == null || huobi.bidPrice().compareTo(bestBid) > 0) {
                bestBid = huobi.bidPrice();
                bestExchange = HUOBI;
            }
        }
        if (price.getBidPrice() != null) {
            if (bestBid == null || price.getBidPrice().compareTo(bestBid) > 0) {
                bestBid = price.getBidPrice();
                bestExchange = price.getBidExchange();
            }
        }

        if (bestBid != null) {
            price.setBidPrice(bestBid);
            price.setBidExchange(bestExchange != null ? bestExchange : BINANCE);
        }
    }

    private void computeBestAsk(AggregatedPriceEntity price, BookTicker binance, BookTicker huobi) {
        BigDecimal bestAsk = null;
        String bestExchange = null;

        if (binance != null) {
            bestAsk = binance.askPrice();
            bestExchange = BINANCE;
        }
        if (huobi != null) {
            if (bestAsk == null || huobi.askPrice().compareTo(bestAsk) < 0) {
                bestAsk = huobi.askPrice();
                bestExchange = HUOBI;
            }
        }
        if (price.getAskPrice() != null) {
            if (bestAsk == null || price.getAskPrice().compareTo(bestAsk) < 0) {
                bestAsk = price.getAskPrice();
                bestExchange = price.getAskExchange();
            }
        }

        if (bestAsk != null) {
            price.setAskPrice(bestAsk);
            price.setAskExchange(bestExchange != null ? bestExchange : BINANCE);
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
