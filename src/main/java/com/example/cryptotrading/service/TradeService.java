package com.example.cryptotrading.service;

import com.example.cryptotrading.dto.TradeRequest;
import com.example.cryptotrading.dto.TradeResponse;
import com.example.cryptotrading.entity.AggregatedPriceEntity;
import com.example.cryptotrading.entity.TradeEntity;
import com.example.cryptotrading.exception.PriceUnavailableException;
import com.example.cryptotrading.repository.TradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
public class TradeService {

    private static final Set<String> SUPPORTED_SYMBOLS = Set.of("BTCUSDT", "ETHUSDT");
    private static final Set<String> VALID_SIDES = Set.of("BUY", "SELL");
    private static final long MAX_PRICE_AGE_SECONDS = 30;

    private final PriceService priceService;
    private final WalletService walletService;
    private final TradeRepository tradeRepository;

    public TradeService(PriceService priceService, WalletService walletService,
                        TradeRepository tradeRepository) {
        this.priceService = priceService;
        this.walletService = walletService;
        this.tradeRepository = tradeRepository;
    }

    @Transactional
    public TradeResponse executeTrade(Long userId, TradeRequest request) {
        String symbol = request.symbol().toUpperCase();
        String side = request.side().toUpperCase();

        validateRequest(symbol, side);

        AggregatedPriceEntity aggregatedPrice = priceService.getLatestPrice(symbol)
                .orElseThrow(() -> new PriceUnavailableException("No price available for " + symbol));

        validatePriceFreshness(aggregatedPrice);

        String cryptoCurrency = extractCryptoCurrency(symbol);
        BigDecimal executionPrice;
        BigDecimal cost;

        if ("BUY".equals(side)) {
            executionPrice = aggregatedPrice.getAskPrice();
            cost = request.quantity().multiply(executionPrice).setScale(8, RoundingMode.HALF_UP);
            walletService.debit(userId, "USDT", cost);
            walletService.credit(userId, cryptoCurrency, request.quantity());
        } else {
            executionPrice = aggregatedPrice.getBidPrice();
            cost = request.quantity().multiply(executionPrice).setScale(8, RoundingMode.HALF_UP);
            walletService.debit(userId, cryptoCurrency, request.quantity());
            walletService.credit(userId, "USDT", cost);
        }

        TradeEntity trade = new TradeEntity(userId, symbol, side, executionPrice, request.quantity(), cost);
        trade = tradeRepository.save(trade);

        return toResponse(trade);
    }

    @Transactional(readOnly = true)
    public List<TradeResponse> getTradeHistory(Long userId) {
        return tradeRepository.findByUserIdOrderByCtlCreTsDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateRequest(String symbol, String side) {
        if (!SUPPORTED_SYMBOLS.contains(symbol)) {
            throw new IllegalArgumentException("Unsupported symbol: " + symbol + ". Supported: " + SUPPORTED_SYMBOLS);
        }
        if (!VALID_SIDES.contains(side)) {
            throw new IllegalArgumentException("Invalid side: " + side + ". Must be BUY or SELL");
        }
    }

    private void validatePriceFreshness(AggregatedPriceEntity price) {
        long ageSeconds = ChronoUnit.SECONDS.between(price.getUpdatedAt(), LocalDateTime.now());
        if (ageSeconds > MAX_PRICE_AGE_SECONDS) {
            throw new PriceUnavailableException(
                    "Price for " + price.getSymbol() + " is stale (last updated " + ageSeconds + "s ago)");
        }
    }

    private String extractCryptoCurrency(String symbol) {
        return symbol.replace("USDT", "");
    }

    private TradeResponse toResponse(TradeEntity trade) {
        return new TradeResponse(
                trade.getId(),
                trade.getSymbol(),
                trade.getSide(),
                trade.getPrice(),
                trade.getQuantity(),
                trade.getCost(),
                trade.getCreatedAt()
        );
    }
}
