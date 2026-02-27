package com.example.cryptotrading;

import com.example.cryptotrading.client.BinanceClient.BookTicker;
import com.example.cryptotrading.entity.AggregatedPriceEntity;
import com.example.cryptotrading.entity.CurrencyEntity;
import com.example.cryptotrading.entity.OrderSideEntity;
import com.example.cryptotrading.entity.TradingPairEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public final class TestFixtures {

    private TestFixtures() {
    }

    // --- IDs ---
    public static final Long BTC_CURRENCY_ID = 1L;
    public static final Long ETH_CURRENCY_ID = 2L;
    public static final Long USDT_CURRENCY_ID = 3L;

    public static final Long BTCUSDT_PAIR_ID = 1L;
    public static final Long ETHUSDT_PAIR_ID = 2L;

    public static final Long BUY_SIDE_ID = 1L;
    public static final Long SELL_SIDE_ID = 2L;

    public static final Long DEFAULT_USER_ID = 1L;

    // --- Symbols & codes ---
    public static final String BTCUSDT = "BTCUSDT";
    public static final String ETHUSDT = "ETHUSDT";
    public static final Set<String> ALL_SYMBOLS = Set.of(BTCUSDT, ETHUSDT);

    public static final String BTC = "BTC";
    public static final String ETH = "ETH";
    public static final String USDT = "USDT";
    public static final String BUY = "BUY";
    public static final String SELL = "SELL";

    public static final String BINANCE = "BINANCE";
    public static final String HUOBI = "HUOBI";

    // --- Common price data ---
    public static final BigDecimal BTC_BID = new BigDecimal("50000");
    public static final BigDecimal BTC_ASK = new BigDecimal("50100");
    public static final BigDecimal ETH_BID = new BigDecimal("3000");
    public static final BigDecimal ETH_ASK = new BigDecimal("3010");

    // --- Currency entities ---
    public static CurrencyEntity btcCurrency() {
        return new CurrencyEntity(BTC_CURRENCY_ID, BTC, "Bitcoin");
    }

    public static CurrencyEntity ethCurrency() {
        return new CurrencyEntity(ETH_CURRENCY_ID, ETH, "Ethereum");
    }

    public static CurrencyEntity usdtCurrency() {
        return new CurrencyEntity(USDT_CURRENCY_ID, USDT, "Tether USD");
    }

    // --- Order side entities ---
    public static OrderSideEntity buySide() {
        return new OrderSideEntity(BUY_SIDE_ID, BUY, "Buy order");
    }

    public static OrderSideEntity sellSide() {
        return new OrderSideEntity(SELL_SIDE_ID, SELL, "Sell order");
    }

    // --- Trading pair entities (with currency relationships set) ---
    public static TradingPairEntity btcusdtPair() {
        TradingPairEntity pair = new TradingPairEntity(BTCUSDT_PAIR_ID, BTCUSDT);
        pair.setBaseCurrency(btcCurrency());
        pair.setQuoteCurrency(usdtCurrency());
        return pair;
    }

    public static TradingPairEntity ethusdtPair() {
        TradingPairEntity pair = new TradingPairEntity(ETHUSDT_PAIR_ID, ETHUSDT);
        pair.setBaseCurrency(ethCurrency());
        pair.setQuoteCurrency(usdtCurrency());
        return pair;
    }

    // --- Lightweight trading pairs (no currency relationships, for PriceServiceTest) ---
    public static TradingPairEntity btcusdtPairRef() {
        return new TradingPairEntity(BTCUSDT_PAIR_ID, BTCUSDT);
    }

    public static TradingPairEntity ethusdtPairRef() {
        return new TradingPairEntity(ETHUSDT_PAIR_ID, ETHUSDT);
    }

    // --- Aggregated price entities ---
    public static AggregatedPriceEntity btcAggregatedPrice() {
        AggregatedPriceEntity price = new AggregatedPriceEntity(
                btcusdtPair(), BTC_BID, BTC_ASK, BINANCE, HUOBI);
        price.setCtlCreTs(LocalDateTime.now());
        return price;
    }

    public static AggregatedPriceEntity ethAggregatedPrice() {
        AggregatedPriceEntity price = new AggregatedPriceEntity(
                ethusdtPair(), ETH_BID, ETH_ASK, HUOBI, BINANCE);
        price.setCtlCreTs(LocalDateTime.now());
        return price;
    }

    public static AggregatedPriceEntity staleBtcPrice() {
        AggregatedPriceEntity price = new AggregatedPriceEntity(
                btcusdtPair(), BTC_BID, BTC_ASK, BINANCE, HUOBI);
        price.setCtlCreTs(LocalDateTime.now().minusSeconds(60));
        return price;
    }

    // --- Book tickers ---
    public static BookTicker btcBinanceTicker() {
        return new BookTicker(BTC_BID, BTC_ASK);
    }

    public static BookTicker ethBinanceTicker() {
        return new BookTicker(ETH_BID, ETH_ASK);
    }
}
