package com.example.cryptotrading.client;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@AllArgsConstructor
@Slf4j
public class BinanceClient {

    private static final String URL = "https://api.binance.com/api/v3/ticker/bookTicker";

    private final RestTemplate restTemplate;

    public Map<String, BookTicker> getBookTickers(Set<String> supportedSymbols) {
        try {
            List<BinanceTicker> response = restTemplate.exchange(
                    URL, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<BinanceTicker>>() {}
            ).getBody();

            if (response == null) {
                return Collections.emptyMap();
            }

            return response.stream()
                    .filter(t -> supportedSymbols.contains(t.symbol()))
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
