package com.example.cryptotrading.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BinanceClient {

    private static final Logger log = LoggerFactory.getLogger(BinanceClient.class);
    private static final String URL = "https://api.binance.com/api/v3/ticker/bookTicker";
    private static final Set<String> SUPPORTED_SYMBOLS = Set.of("BTCUSDT", "ETHUSDT");

    private final RestTemplate restTemplate;

    public BinanceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, BookTicker> getBookTickers() {
        try {
            List<BinanceTicker> response = restTemplate.exchange(
                    URL, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<BinanceTicker>>() {}
            ).getBody();

            if (response == null) {
                return Collections.emptyMap();
            }

            return response.stream()
                    .filter(t -> SUPPORTED_SYMBOLS.contains(t.symbol()))
                    .collect(Collectors.toMap(
                            BinanceTicker::symbol,
                            t -> new BookTicker(new BigDecimal(t.bidPrice()), new BigDecimal(t.askPrice()))
                    ));
        } catch (Exception e) {
            log.warn("Failed to fetch Binance tickers: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BinanceTicker(String symbol, String bidPrice, String askPrice) {
    }

    public record BookTicker(BigDecimal bidPrice, BigDecimal askPrice) {
    }
}
