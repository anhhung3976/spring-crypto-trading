package com.example.cryptotrading.service;

import com.example.cryptotrading.client.BinanceClient;
import com.example.cryptotrading.client.BinanceClient.BookTicker;
import com.example.cryptotrading.client.HuobiClient;
import com.example.cryptotrading.entity.AggregatedPriceEntity;
import com.example.cryptotrading.repository.AggregatedPriceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

    @Mock
    private BinanceClient binanceClient;

    @Mock
    private HuobiClient huobiClient;

    @Mock
    private AggregatedPriceRepository priceRepository;

    @InjectMocks
    private PriceService priceService;

    @Test
    void aggregatePrices_selectsBestFromBothExchanges() {
        Map<String, BookTicker> binance = Map.of(
                "BTCUSDT", new BookTicker(new BigDecimal("50000"), new BigDecimal("50100")),
                "ETHUSDT", new BookTicker(new BigDecimal("3000"), new BigDecimal("3010"))
        );
        Map<String, BookTicker> huobi = Map.of(
                "BTCUSDT", new BookTicker(new BigDecimal("50050"), new BigDecimal("50080")),
                "ETHUSDT", new BookTicker(new BigDecimal("2990"), new BigDecimal("3005"))
        );

        when(binanceClient.getBookTickers()).thenReturn(binance);
        when(huobiClient.getBookTickers()).thenReturn(huobi);
        when(priceRepository.findBySymbol(any())).thenReturn(Optional.empty());
        when(priceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        priceService.aggregatePrices();

        ArgumentCaptor<AggregatedPriceEntity> captor = ArgumentCaptor.forClass(AggregatedPriceEntity.class);
        verify(priceRepository, times(2)).save(captor.capture());

        var savedPrices = captor.getAllValues();

        AggregatedPriceEntity btc = savedPrices.stream()
                .filter(p -> "BTCUSDT".equals(p.getSymbol())).findFirst().orElseThrow();
        assertEquals(new BigDecimal("50050"), btc.getBidPrice());
        assertEquals("HUOBI", btc.getBidExchange());
        assertEquals(new BigDecimal("50080"), btc.getAskPrice());
        assertEquals("HUOBI", btc.getAskExchange());

        AggregatedPriceEntity eth = savedPrices.stream()
                .filter(p -> "ETHUSDT".equals(p.getSymbol())).findFirst().orElseThrow();
        assertEquals(new BigDecimal("3000"), eth.getBidPrice());
        assertEquals("BINANCE", eth.getBidExchange());
        assertEquals(new BigDecimal("3005"), eth.getAskPrice());
        assertEquals("HUOBI", eth.getAskExchange());
    }

    @Test
    void aggregatePrices_onlyOneExchangeAvailable_usesThatExchange() {
        Map<String, BookTicker> binance = Map.of(
                "BTCUSDT", new BookTicker(new BigDecimal("50000"), new BigDecimal("50100"))
        );

        when(binanceClient.getBookTickers()).thenReturn(binance);
        when(huobiClient.getBookTickers()).thenReturn(Collections.emptyMap());
        when(priceRepository.findBySymbol("BTCUSDT")).thenReturn(Optional.empty());
        when(priceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        priceService.aggregatePrices();

        ArgumentCaptor<AggregatedPriceEntity> captor = ArgumentCaptor.forClass(AggregatedPriceEntity.class);
        verify(priceRepository).save(captor.capture());

        AggregatedPriceEntity saved = captor.getValue();
        assertEquals("BTCUSDT", saved.getSymbol());
        assertEquals(new BigDecimal("50000"), saved.getBidPrice());
        assertEquals(new BigDecimal("50100"), saved.getAskPrice());
        assertEquals("BINANCE", saved.getBidExchange());
        assertEquals("BINANCE", saved.getAskExchange());
    }

    @Test
    void aggregatePrices_bothExchangesFail_skipsSave() {
        when(binanceClient.getBookTickers()).thenReturn(Collections.emptyMap());
        when(huobiClient.getBookTickers()).thenReturn(Collections.emptyMap());

        priceService.aggregatePrices();

        verify(priceRepository, never()).save(any());
    }
}
