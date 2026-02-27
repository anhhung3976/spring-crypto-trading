package com.example.cryptotrading.client;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class HuobiClient {

    private static final String URL = "https://api.huobi.pro/market/tickers";
    private static final Set<String> SUPPORTED_SYMBOLS = Set.of("btcusdt", "ethusdt");

    private final RestTemplate restTemplate;

    public HuobiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, BinanceClient.BookTicker> getBookTickers() {
        try {
            HuobiResponse response = restTemplate.getForObject(URL, HuobiResponse.class);

            if (response == null || response.data() == null) {
                return Collections.emptyMap();
            }

            return response.data().stream()
                    .filter(t -> SUPPORTED_SYMBOLS.contains(t.symbol()))
                    .collect(Collectors.toMap(
                            t -> t.symbol().toUpperCase(),
                            t -> new BinanceClient.BookTicker(t.bid(), t.ask())
                    ));
        } catch (Exception e) {
            log.warn("Failed to fetch Huobi tickers: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HuobiResponse(String status, List<HuobiTicker> data) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HuobiTicker(String symbol, BigDecimal bid, BigDecimal ask) {
    }
}
