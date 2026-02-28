package com.example.cryptotrading.service;

import com.example.cryptotrading.client.BinanceClient;
import com.example.cryptotrading.client.BinanceClient.BookTicker;
import com.example.cryptotrading.client.HuobiClient;
import com.example.cryptotrading.entity.AggregatedPriceEntity;
import com.example.cryptotrading.entity.TradingPairEntity;
import com.example.cryptotrading.repository.AggregatedPriceRepository;
import com.example.cryptotrading.repository.TradingPairRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Optional;

import static com.example.cryptotrading.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Mock
    private TradingPairRepository tradingPairRepository;

    @InjectMocks
    private PriceService priceService;

    private void mockActivePairs(TradingPairEntity... pairs) {
        when(tradingPairRepository.findByCtlActTrue()).thenReturn(List.of(pairs));
    }

    @Test
    void aggregatePrices_selectsBestFromBothExchanges() {
        mockActivePairs(btcusdtPairRef(), ethusdtPairRef());

        Map<String, BookTicker> binance = Map.of(
                BTCUSDT, new BookTicker(new BigDecimal("50000"), new BigDecimal("50100")),
                ETHUSDT, new BookTicker(new BigDecimal("3000"), new BigDecimal("3010"))
        );
        Map<String, BookTicker> huobi = Map.of(
                BTCUSDT, new BookTicker(new BigDecimal("50050"), new BigDecimal("50080")),
                ETHUSDT, new BookTicker(new BigDecimal("2990"), new BigDecimal("3005"))
        );

        when(binanceClient.getBookTickers(ALL_SYMBOLS)).thenReturn(binance);
        when(huobiClient.getBookTickers(ALL_SYMBOLS)).thenReturn(huobi);
        when(priceRepository.findByTradingPair(any())).thenReturn(Optional.empty());
        when(priceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        priceService.aggregatePrices();

        ArgumentCaptor<AggregatedPriceEntity> captor = ArgumentCaptor.forClass(AggregatedPriceEntity.class);
        verify(priceRepository, times(2)).save(captor.capture());

        var savedPrices = captor.getAllValues();

        AggregatedPriceEntity btc = savedPrices.stream()
                .filter(p -> p.getTradingPair().getId().equals(BTCUSDT_PAIR_ID)).findFirst().orElseThrow();
        assertEquals(new BigDecimal("50050"), btc.getBidPrice());
        assertEquals(HUOBI, btc.getBidExchange());
        assertEquals(new BigDecimal("50080"), btc.getAskPrice());
        assertEquals(HUOBI, btc.getAskExchange());

        AggregatedPriceEntity eth = savedPrices.stream()
                .filter(p -> p.getTradingPair().getId().equals(ETHUSDT_PAIR_ID)).findFirst().orElseThrow();
        assertEquals(new BigDecimal("3000"), eth.getBidPrice());
        assertEquals(BINANCE, eth.getBidExchange());
        assertEquals(new BigDecimal("3005"), eth.getAskPrice());
        assertEquals(HUOBI, eth.getAskExchange());
    }

    @Test
    void aggregatePrices_onlyOneExchangeAvailable_usesThatExchange() {
        mockActivePairs(btcusdtPairRef(), ethusdtPairRef());

        Map<String, BookTicker> binance = Map.of(BTCUSDT, btcBinanceTicker());

        when(binanceClient.getBookTickers(ALL_SYMBOLS)).thenReturn(binance);
        when(huobiClient.getBookTickers(ALL_SYMBOLS)).thenReturn(Collections.emptyMap());
        when(priceRepository.findByTradingPair(any())).thenReturn(Optional.empty());
        when(priceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        priceService.aggregatePrices();

        ArgumentCaptor<AggregatedPriceEntity> captor = ArgumentCaptor.forClass(AggregatedPriceEntity.class);
        verify(priceRepository).save(captor.capture());

        AggregatedPriceEntity saved = captor.getValue();
        assertEquals(BTCUSDT_PAIR_ID, saved.getTradingPair().getId());
        assertEquals(BTC_BID, saved.getBidPrice());
        assertEquals(BTC_ASK, saved.getAskPrice());
        assertEquals(BINANCE, saved.getBidExchange());
        assertEquals(BINANCE, saved.getAskExchange());
    }

    @Test
    void aggregatePrices_comparesWithStoredPrice_andKeepsBest() {
        mockActivePairs(btcusdtPairRef());

        // Binance: bid 50000, ask 50100; Huobi: bid 50050, ask 50080
        // Without DB: best bid=50050 (Huobi), best ask=50080 (Huobi)
        Map<String, BookTicker> binance = Map.of(
                BTCUSDT, new BookTicker(new BigDecimal("50000"), new BigDecimal("50100"))
        );
        Map<String, BookTicker> huobi = Map.of(
                BTCUSDT, new BookTicker(new BigDecimal("50050"), new BigDecimal("50080"))
        );

        // Stored price has better bid (50100) and better ask (49990) than both exchanges
        AggregatedPriceEntity storedPrice = new AggregatedPriceEntity(
                btcusdtPairRef(),
                new BigDecimal("50100"), new BigDecimal("49990"),
                BINANCE, HUOBI);
        storedPrice.setCtlCreTs(LocalDateTime.now().minusSeconds(1));
        storedPrice.setCtlModTs(LocalDateTime.now());

        when(binanceClient.getBookTickers(Set.of(BTCUSDT))).thenReturn(binance);
        when(huobiClient.getBookTickers(Set.of(BTCUSDT))).thenReturn(huobi);
        when(priceRepository.findByTradingPair(any())).thenReturn(Optional.of(storedPrice));
        when(priceRepository.touchLastChecked(any(), any())).thenReturn(1);

        priceService.aggregatePrices();

        // Stored price was best; only touchLastChecked (no full save)
        verify(priceRepository).touchLastChecked(any(), any());
        verify(priceRepository, never()).save(any());
    }

    @Test
    void aggregatePrices_bothExchangesFail_skipsTouchAndSave() {
        mockActivePairs(btcusdtPairRef(), ethusdtPairRef());

        when(binanceClient.getBookTickers(ALL_SYMBOLS)).thenReturn(Collections.emptyMap());
        when(huobiClient.getBookTickers(ALL_SYMBOLS)).thenReturn(Collections.emptyMap());

        priceService.aggregatePrices();

        verify(priceRepository, never()).touchLastChecked(any(), any());
        verify(priceRepository, never()).save(any());
    }

    @Test
    void aggregatePrices_noActivePairs_skipsAggregation() {
        when(tradingPairRepository.findByCtlActTrue()).thenReturn(Collections.emptyList());

        priceService.aggregatePrices();

        verify(binanceClient, never()).getBookTickers(any());
        verify(huobiClient, never()).getBookTickers(any());
        verify(priceRepository, never()).save(any());
    }

    @Test
    void getLatestPrice_delegatesToRepository() {
        AggregatedPriceEntity entity = btcAggregatedPrice();
        when(priceRepository.findByTradingPairSymbol(BTCUSDT)).thenReturn(Optional.of(entity));

        Optional<AggregatedPriceEntity> result = priceService.getLatestPrice(BTCUSDT);

        assertTrue(result.isPresent());
        assertEquals(BTC_BID, result.get().getBidPrice());
    }
}
