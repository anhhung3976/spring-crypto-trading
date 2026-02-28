package com.example.cryptotrading.service;

import com.example.cryptotrading.domain.OrderSideCodeEnum;
import com.example.cryptotrading.dto.GenericPage;
import com.example.cryptotrading.dto.TradeHistoryDto;
import com.example.cryptotrading.dto.TradeHistoryFilterDto;
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
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
        OrderSideCodeEnum side = request.side();

        TradingPairEntity tradingPair = tradingPairRepository.findBySymbol(symbol)
                .filter(TradingPairEntity::getCtlAct)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported trading pair: " + symbol));

        OrderSideEntity orderSide = orderSideRepository.findByCode(side)
                .orElseThrow(() -> new IllegalArgumentException("Invalid side: " + side + ". Must be BUY or SELL"));

        AggregatedPriceEntity aggregatedPrice = priceService.getLatestPrice(symbol)
                .orElseThrow(() -> new PriceUnavailableException("No price available for " + symbol));

        validatePriceFreshness(aggregatedPrice, symbol);

        String baseCurrencyCode = tradingPair.getBaseCurrency().getCode();
        String quoteCurrencyCode = tradingPair.getQuoteCurrency().getCode();
        BigDecimal executionPrice;
        BigDecimal cost;

        if (OrderSideCodeEnum.BUY == side) {
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

        BigDecimal currentBaseBalance = walletService.getBalance(userId, baseCurrencyCode);

        return toResponse(trade, symbol, side, currentBaseBalance);
    }

    @Transactional(readOnly = true)
    public GenericPage<TradeHistoryDto> getTradeHistory(Long userId, TradeHistoryFilterDto filter) {
        Page<TradeEntity> page = tradeRepository.findByUserIdOrderByCtlCreTsDesc(userId, filter.toPageableOrDefault());
        var data = page.getContent().stream()
                .map(trade -> new TradeHistoryDto(
                        trade.getId(),
                        trade.getTradingPair().getSymbol(),
                        trade.getOrderSide().getCode(),
                        trade.getPrice(),
                        trade.getQuantity(),
                        trade.getCost(),
                        trade.getCtlCreTs()
                ))
                .toList();
        return new GenericPage<>(
                data,
                page.getTotalElements(),
                page.getNumber(),
                page.getSize()
        );
    }

    private void validatePriceFreshness(AggregatedPriceEntity price, String symbol) {
        LocalDateTime lastChecked = price.getLastCheckedAt() != null
                ? price.getLastCheckedAt()
                : price.getCtlCreTs();
        if (lastChecked == null) {
            throw new PriceUnavailableException("Price for " + symbol + " has no timestamp");
        }
        long ageSeconds = ChronoUnit.SECONDS.between(lastChecked, LocalDateTime.now());
        if (ageSeconds < 0) {
            ageSeconds = 0; // clock skew
        }
        if (ageSeconds > MAX_PRICE_AGE_SECONDS) {
            throw new PriceUnavailableException(
                    "Price for " + symbol + " is stale (last checked " + ageSeconds + "s ago)");
        }
    }

    private TradeResponseDto toResponse(TradeEntity trade, String symbol, OrderSideCodeEnum side, BigDecimal currentBalance) {
        return new TradeResponseDto(
                trade.getId(),
                symbol,
                side,
                trade.getPrice(),
                trade.getQuantity(),
                trade.getCost(),
                currentBalance,
                trade.getCtlCreTs()
        );
    }
}
