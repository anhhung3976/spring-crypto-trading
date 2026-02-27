package com.example.cryptotrading.service;

import com.example.cryptotrading.dto.TradeRequest;
import com.example.cryptotrading.dto.TradeResponse;
import com.example.cryptotrading.entity.AggregatedPrice;
import com.example.cryptotrading.exception.InsufficientBalanceException;
import com.example.cryptotrading.exception.PriceUnavailableException;
import com.example.cryptotrading.repository.TradeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private PriceService priceService;

    @Mock
    private WalletService walletService;

    @Mock
    private TradeRepository tradeRepository;

    @InjectMocks
    private TradeService tradeService;

    private static final Long USER_ID = 1L;

    @Test
    void executeBuyTrade_success() {
        AggregatedPrice price = new AggregatedPrice(
                "BTCUSDT", new BigDecimal("50000"), new BigDecimal("50100"),
                "BINANCE", "HUOBI", LocalDateTime.now());
        when(priceService.getLatestPrice("BTCUSDT")).thenReturn(Optional.of(price));
        doNothing().when(walletService).debit(any(), any(), any());
        doNothing().when(walletService).credit(any(), any(), any());
        when(tradeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TradeRequest request = new TradeRequest("BTCUSDT", "BUY", new BigDecimal("0.5"));
        TradeResponse response = tradeService.executeTrade(USER_ID, request);

        assertNotNull(response);
        assertEquals("BTCUSDT", response.symbol());
        assertEquals("BUY", response.side());
        assertEquals(new BigDecimal("50100"), response.price());
        assertEquals(new BigDecimal("0.5"), response.quantity());

        verify(walletService).debit(USER_ID, "USDT", new BigDecimal("25050.00000000"));
        verify(walletService).credit(USER_ID, "BTC", new BigDecimal("0.5"));
    }

    @Test
    void executeSellTrade_success() {
        AggregatedPrice price = new AggregatedPrice(
                "ETHUSDT", new BigDecimal("3000"), new BigDecimal("3010"),
                "HUOBI", "BINANCE", LocalDateTime.now());
        when(priceService.getLatestPrice("ETHUSDT")).thenReturn(Optional.of(price));
        doNothing().when(walletService).debit(any(), any(), any());
        doNothing().when(walletService).credit(any(), any(), any());
        when(tradeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TradeRequest request = new TradeRequest("ETHUSDT", "SELL", new BigDecimal("2"));
        TradeResponse response = tradeService.executeTrade(USER_ID, request);

        assertNotNull(response);
        assertEquals("ETHUSDT", response.symbol());
        assertEquals("SELL", response.side());
        assertEquals(new BigDecimal("3000"), response.price());

        verify(walletService).debit(USER_ID, "ETH", new BigDecimal("2"));
        verify(walletService).credit(USER_ID, "USDT", new BigDecimal("6000.00000000"));
    }

    @Test
    void executeTrade_unsupportedSymbol_throwsException() {
        TradeRequest request = new TradeRequest("DOGEUSDT", "BUY", new BigDecimal("100"));

        assertThrows(IllegalArgumentException.class,
                () -> tradeService.executeTrade(USER_ID, request));
    }

    @Test
    void executeTrade_invalidSide_throwsException() {
        TradeRequest request = new TradeRequest("BTCUSDT", "HOLD", new BigDecimal("1"));

        assertThrows(IllegalArgumentException.class,
                () -> tradeService.executeTrade(USER_ID, request));
    }

    @Test
    void executeTrade_priceUnavailable_throwsException() {
        when(priceService.getLatestPrice("BTCUSDT")).thenReturn(Optional.empty());

        TradeRequest request = new TradeRequest("BTCUSDT", "BUY", new BigDecimal("1"));

        assertThrows(PriceUnavailableException.class,
                () -> tradeService.executeTrade(USER_ID, request));
    }

    @Test
    void executeTrade_stalePrice_throwsException() {
        AggregatedPrice stalePrice = new AggregatedPrice(
                "BTCUSDT", new BigDecimal("50000"), new BigDecimal("50100"),
                "BINANCE", "HUOBI", LocalDateTime.now().minusSeconds(60));
        when(priceService.getLatestPrice("BTCUSDT")).thenReturn(Optional.of(stalePrice));

        TradeRequest request = new TradeRequest("BTCUSDT", "BUY", new BigDecimal("1"));

        assertThrows(PriceUnavailableException.class,
                () -> tradeService.executeTrade(USER_ID, request));
    }

    @Test
    void executeBuyTrade_insufficientBalance_throwsException() {
        AggregatedPrice price = new AggregatedPrice(
                "BTCUSDT", new BigDecimal("50000"), new BigDecimal("50100"),
                "BINANCE", "HUOBI", LocalDateTime.now());
        when(priceService.getLatestPrice("BTCUSDT")).thenReturn(Optional.of(price));
        doThrow(new InsufficientBalanceException("Insufficient USDT balance"))
                .when(walletService).debit(any(), any(), any());

        TradeRequest request = new TradeRequest("BTCUSDT", "BUY", new BigDecimal("1000"));

        assertThrows(InsufficientBalanceException.class,
                () -> tradeService.executeTrade(USER_ID, request));
    }
}
