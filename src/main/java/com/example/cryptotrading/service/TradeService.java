package com.example.cryptotrading.service;

import com.example.cryptotrading.dto.TradeRequestDto;
import com.example.cryptotrading.dto.TradeResponseDto;
import com.example.cryptotrading.entity.AggregatedPriceEntity;
import com.example.cryptotrading.entity.OrderSideEntity;
import com.example.cryptotrading.entity.TradingPairEntity;
import com.example.cryptotrading.entity.TradeEntity;
import com.example.cryptotrading.exception.PriceUnavailableException;
import com.example.cryptotrading.repository.OrderSideRepository;
import com.example.cryptotrading.repository.TradeRepository;
import com.example.cryptotrading.repository.TradingPairRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@AllArgsConstructor
public class TradeService {

    private static final long MAX_PRICE_AGE_SECONDS = 30;

    private final PriceService priceService;
    private final WalletService walletService;
    private final TradeRepository tradeRepository;
    private final TradingPairRepository tradingPairRepository;
    private final OrderSideRepository orderSideRepository;

    @Transactional
    public TradeResponseDto executeTrade(Long userId, TradeRequestDto request) {
        String symbol = request.symbol().toUpperCase();
        String sideCode = request.side().toUpperCase();

        TradingPairEntity tradingPair = tradingPairRepository.findBySymbol(symbol)
                .filter(TradingPairEntity::getCtlAct)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported trading pair: " + symbol));

        OrderSideEntity orderSide = orderSideRepository.findByCode(sideCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid side: " + sideCode + ". Must be BUY or SELL"));

        AggregatedPriceEntity aggregatedPrice = priceService.getLatestPrice(symbol)
                .orElseThrow(() -> new PriceUnavailableException("No price available for " + symbol));

        validatePriceFreshness(aggregatedPrice, symbol);

        String baseCurrencyCode = tradingPair.getBaseCurrency().getCode();
        String quoteCurrencyCode = tradingPair.getQuoteCurrency().getCode();
        BigDecimal executionPrice;
        BigDecimal cost;

        if ("BUY".equals(sideCode)) {
            executionPrice = aggregatedPrice.getAskPrice();
            cost = request.quantity().multiply(executionPrice).setScale(8, RoundingMode.HALF_UP);
            walletService.debit(userId, quoteCurrencyCode, cost);
            walletService.credit(userId, baseCurrencyCode, request.quantity());
        } else {
            executionPrice = aggregatedPrice.getBidPrice();
            cost = request.quantity().multiply(executionPrice).setScale(8, RoundingMode.HALF_UP);
            walletService.debit(userId, baseCurrencyCode, request.quantity());
            walletService.credit(userId, quoteCurrencyCode, cost);
        }

        TradeEntity trade = new TradeEntity(
                userId, tradingPair, orderSide,
                executionPrice, request.quantity(), cost);
        trade = tradeRepository.save(trade);

        return toResponse(trade, symbol, sideCode);
    }

    @Transactional(readOnly = true)
    public List<TradeResponseDto> getTradeHistory(Long userId) {
        return tradeRepository.findByUserIdOrderByCtlCreTsDesc(userId).stream()
                .map(trade -> toResponse(
                        trade,
                        trade.getTradingPair().getSymbol(),
                        trade.getOrderSide().getCode()))
                .toList();
    }

    private void validatePriceFreshness(AggregatedPriceEntity price, String symbol) {
        long ageSeconds = ChronoUnit.SECONDS.between(price.getUpdatedAt(), LocalDateTime.now());
        if (ageSeconds > MAX_PRICE_AGE_SECONDS) {
            throw new PriceUnavailableException(
                    "Price for " + symbol + " is stale (last updated " + ageSeconds + "s ago)");
        }
    }

    private TradeResponseDto toResponse(TradeEntity trade, String symbol, String side) {
        return new TradeResponseDto(
                trade.getId(),
                symbol,
                side,
                trade.getPrice(),
                trade.getQuantity(),
                trade.getCost(),
                trade.getCreatedAt()
        );
    }
}
