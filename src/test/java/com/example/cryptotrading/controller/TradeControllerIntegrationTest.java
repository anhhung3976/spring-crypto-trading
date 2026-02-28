package com.example.cryptotrading.controller;

import com.example.cryptotrading.entity.AggregatedPriceEntity;
import com.example.cryptotrading.repository.AggregatedPriceRepository;
import com.example.cryptotrading.repository.TradingPairRepository;
import com.example.cryptotrading.repository.TradeRepository;
import com.example.cryptotrading.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.example.cryptotrading.TestFixtures.*;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.scheduling.enabled=false")
class TradeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AggregatedPriceRepository priceRepository;

    @Autowired
    private TradingPairRepository tradingPairRepository;
    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private WalletRepository walletRepository;

    @BeforeEach
    void setUp() {
        tradeRepository.deleteAll();
        priceRepository.deleteAll();

        resetWalletBalance(USDT, new BigDecimal("50000.00000000"));
        resetWalletBalance(BTC, new BigDecimal("0.00000000"));
        resetWalletBalance(ETH, new BigDecimal("0.00000000"));

        var btcPair = tradingPairRepository.findById(BTCUSDT_PAIR_ID).orElseThrow();
        var ethPair = tradingPairRepository.findById(ETHUSDT_PAIR_ID).orElseThrow();

        var btcPrice = new AggregatedPriceEntity(btcPair, BTC_BID, BTC_ASK, BINANCE, HUOBI);
        btcPrice.setCtlCreTs(LocalDateTime.now());
        btcPrice.setLastCheckedAt(LocalDateTime.now());
        priceRepository.save(btcPrice);

        var ethPrice = new AggregatedPriceEntity(ethPair, ETH_BID, ETH_ASK, HUOBI, BINANCE);
        ethPrice.setCtlCreTs(LocalDateTime.now());
        ethPrice.setLastCheckedAt(LocalDateTime.now());
        priceRepository.save(ethPrice);
    }

    private void resetWalletBalance(String currencyCode, BigDecimal balance) {
        walletRepository.findByUserIdAndCurrencyCode(DEFAULT_USER_ID, currencyCode).ifPresent(wallet -> {
            wallet.setBalance(balance);
            walletRepository.save(wallet);
        });
    }

    @Test
    void getLatestPrices_returnsAllPrices() throws Exception {
        mockMvc.perform(get("/api/prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void executeBuyTrade_returnsCreated() throws Exception {
        String requestBody = """
                {
                    "symbol": "%s",
                    "side": "%s",
                    "quantity": 0.1
                }
                """.formatted(BTCUSDT, BUY);

        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.symbol").value(BTCUSDT))
                .andExpect(jsonPath("$.side").value(BUY.name()))
                .andExpect(jsonPath("$.price").value(BTC_ASK.intValue()))
                .andExpect(jsonPath("$.quantity").value(0.1));
    }

    @Test
    void executeBuyTrade_thenCheckWalletBalance() throws Exception {
        String buyRequest = """
                {
                    "symbol": "%s",
                    "side": "%s",
                    "quantity": 2
                }
                """.formatted(ETHUSDT, BUY);

        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buyRequest))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/wallets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[?(@.currency == 'ETH')].balance").value("2.00000000"));
    }

    @Test
    void executeTrade_invalidSymbol_returnsBadRequest() throws Exception {
        String requestBody = """
                {
                    "symbol": "DOGEUSDT",
                    "side": "%s",
                    "quantity": 100
                }
                """.formatted(BUY);

        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTradeHistory_afterTrade_returnsTrades() throws Exception {
        String buyRequest = """
                {
                    "symbol": "%s",
                    "side": "%s",
                    "quantity": 0.01
                }
                """.formatted(BTCUSDT, BUY);

        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buyRequest))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/trades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].symbol").value(BTCUSDT))
                .andExpect(jsonPath("$.data[0].side").value(BUY.name()))
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.pageNumber").exists())
                .andExpect(jsonPath("$.pageSize").exists());
    }

    @Test
    void getWalletBalances_returnsInitialBalances() throws Exception {
        mockMvc.perform(get("/api/wallets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[?(@.currency == 'USDT')].balance").value("50000.00000000"));
    }
}
