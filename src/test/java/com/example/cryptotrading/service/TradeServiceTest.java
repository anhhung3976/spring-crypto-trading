package com.example.cryptotrading.service;

import com.example.cryptotrading.dto.TradeRequestDto;
import com.example.cryptotrading.dto.TradeResponseDto;
import com.example.cryptotrading.exception.InsufficientBalanceException;
import com.example.cryptotrading.exception.PriceUnavailableException;
import com.example.cryptotrading.repository.OrderSideRepository;
import com.example.cryptotrading.repository.TradeRepository;
import com.example.cryptotrading.repository.TradingPairRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static com.example.cryptotrading.TestFixtures.*;
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

    @Mock
    private TradingPairRepository tradingPairRepository;

    @Mock
    private OrderSideRepository orderSideRepository;

    @InjectMocks
    private TradeService tradeService;

    private void mockBtcPair() {
        when(tradingPairRepository.findBySymbol(BTCUSDT)).thenReturn(Optional.of(btcusdtPair()));
    }

    private void mockEthPair() {
        when(tradingPairRepository.findBySymbol(ETHUSDT)).thenReturn(Optional.of(ethusdtPair()));
    }

    private void mockBuySide() {
        when(orderSideRepository.findByCode(BUY)).thenReturn(Optional.of(buySide()));
    }

    private void mockSellSide() {
        when(orderSideRepository.findByCode(SELL)).thenReturn(Optional.of(sellSide()));
    }

    private void mockFreshBtcPrice() {
        when(priceService.getLatestPrice(BTCUSDT)).thenReturn(Optional.of(btcAggregatedPrice()));
    }

    private void mockWalletAndSave() {
        doNothing().when(walletService).debit(any(), any(), any());
        doNothing().when(walletService).credit(any(), any(), any());
        when(tradeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void executeBuyTrade_success() {
        mockBtcPair();
        mockBuySide();
        mockFreshBtcPrice();
        mockWalletAndSave();

        TradeRequestDto request = new TradeRequestDto(BTCUSDT, BUY, new BigDecimal("0.5"));
        TradeResponseDto response = tradeService.executeTrade(DEFAULT_USER_ID, request);

        assertNotNull(response);
        assertEquals(BTCUSDT, response.symbol());
        assertEquals(BUY, response.side());
        assertEquals(BTC_ASK, response.price());
        assertEquals(new BigDecimal("0.5"), response.quantity());

        verify(walletService).debit(DEFAULT_USER_ID, USDT, new BigDecimal("25050.00000000"));
        verify(walletService).credit(DEFAULT_USER_ID, BTC, new BigDecimal("0.5"));
    }

    @Test
    void executeSellTrade_success() {
        mockEthPair();
        mockSellSide();
        when(priceService.getLatestPrice(ETHUSDT)).thenReturn(Optional.of(ethAggregatedPrice()));
        mockWalletAndSave();

        TradeRequestDto request = new TradeRequestDto(ETHUSDT, SELL, new BigDecimal("2"));
        TradeResponseDto response = tradeService.executeTrade(DEFAULT_USER_ID, request);

        assertNotNull(response);
        assertEquals(ETHUSDT, response.symbol());
        assertEquals(SELL, response.side());
        assertEquals(ETH_BID, response.price());

        verify(walletService).debit(DEFAULT_USER_ID, ETH, new BigDecimal("2"));
        verify(walletService).credit(DEFAULT_USER_ID, USDT, new BigDecimal("6000.00000000"));
    }

    @Test
    void executeTrade_unsupportedSymbol_throwsException() {
        when(tradingPairRepository.findBySymbol("DOGEUSDT")).thenReturn(Optional.empty());

        TradeRequestDto request = new TradeRequestDto("DOGEUSDT", BUY, new BigDecimal("100"));

        assertThrows(IllegalArgumentException.class,
                () -> tradeService.executeTrade(DEFAULT_USER_ID, request));
    }

    @Test
    void executeTrade_invalidSide_throwsException() {
        mockBtcPair();
        when(orderSideRepository.findByCode(BUY)).thenReturn(Optional.empty());

        TradeRequestDto request = new TradeRequestDto(BTCUSDT, BUY, new BigDecimal("1"));

        assertThrows(IllegalArgumentException.class,
                () -> tradeService.executeTrade(DEFAULT_USER_ID, request));
    }

    @Test
    void executeTrade_priceUnavailable_throwsException() {
        mockBtcPair();
        mockBuySide();
        when(priceService.getLatestPrice(BTCUSDT)).thenReturn(Optional.empty());

        TradeRequestDto request = new TradeRequestDto(BTCUSDT, BUY, new BigDecimal("1"));

        assertThrows(PriceUnavailableException.class,
                () -> tradeService.executeTrade(DEFAULT_USER_ID, request));
    }

    @Test
    void executeTrade_stalePrice_throwsException() {
        mockBtcPair();
        mockBuySide();
        when(priceService.getLatestPrice(BTCUSDT)).thenReturn(Optional.of(staleBtcPrice()));

        TradeRequestDto request = new TradeRequestDto(BTCUSDT, BUY, new BigDecimal("1"));

        assertThrows(PriceUnavailableException.class,
                () -> tradeService.executeTrade(DEFAULT_USER_ID, request));
    }

    @Test
    void executeBuyTrade_insufficientBalance_throwsException() {
        mockBtcPair();
        mockBuySide();
        mockFreshBtcPrice();
        doThrow(new InsufficientBalanceException("Insufficient USDT balance"))
                .when(walletService).debit(any(), any(), any());

        TradeRequestDto request = new TradeRequestDto(BTCUSDT, BUY, new BigDecimal("1000"));

        assertThrows(InsufficientBalanceException.class,
                () -> tradeService.executeTrade(DEFAULT_USER_ID, request));
    }
}
