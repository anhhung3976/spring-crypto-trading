package com.example.cryptotrading.controller;

import com.example.cryptotrading.entity.AggregatedPrice;
import com.example.cryptotrading.repository.AggregatedPriceRepository;
import com.example.cryptotrading.repository.TradeRepository;
import com.example.cryptotrading.repository.WalletRepository;
import com.example.cryptotrading.entity.Wallet;
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
    private TradeRepository tradeRepository;

    @Autowired
    private WalletRepository walletRepository;

    @BeforeEach
    void setUp() {
        tradeRepository.deleteAll();
        priceRepository.deleteAll();

        resetWalletBalance("USDT", new BigDecimal("50000.00000000"));
        resetWalletBalance("BTC", new BigDecimal("0.00000000"));
        resetWalletBalance("ETH", new BigDecimal("0.00000000"));

        priceRepository.save(new AggregatedPrice(
                "BTCUSDT", new BigDecimal("50000"), new BigDecimal("50100"),
                "BINANCE", "HUOBI", LocalDateTime.now()));
        priceRepository.save(new AggregatedPrice(
                "ETHUSDT", new BigDecimal("3000"), new BigDecimal("3010"),
                "HUOBI", "BINANCE", LocalDateTime.now()));
    }

    private void resetWalletBalance(String currency, BigDecimal balance) {
        walletRepository.findByUserIdAndCurrency(1L, currency).ifPresent(wallet -> {
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
                    "symbol": "BTCUSDT",
                    "side": "BUY",
                    "quantity": 0.1
                }
                """;

        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.symbol").value("BTCUSDT"))
                .andExpect(jsonPath("$.side").value("BUY"))
                .andExpect(jsonPath("$.price").value(50100))
                .andExpect(jsonPath("$.quantity").value(0.1));
    }

    @Test
    void executeBuyTrade_thenCheckWalletBalance() throws Exception {
        String buyRequest = """
                {
                    "symbol": "ETHUSDT",
                    "side": "BUY",
                    "quantity": 2
                }
                """;

        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buyRequest))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/wallets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[?(@.currency == 'ETH')].balance").value(2.0));
    }

    @Test
    void executeTrade_invalidSymbol_returnsBadRequest() throws Exception {
        String requestBody = """
                {
                    "symbol": "DOGEUSDT",
                    "side": "BUY",
                    "quantity": 100
                }
                """;

        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTradeHistory_afterTrade_returnsTrades() throws Exception {
        String buyRequest = """
                {
                    "symbol": "BTCUSDT",
                    "side": "BUY",
                    "quantity": 0.01
                }
                """;

        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buyRequest))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/trades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].symbol").value("BTCUSDT"))
                .andExpect(jsonPath("$[0].side").value("BUY"));
    }

    @Test
    void getWalletBalances_returnsInitialBalances() throws Exception {
        mockMvc.perform(get("/api/wallets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[?(@.currency == 'USDT')].balance").value(50000.0));
    }
}
