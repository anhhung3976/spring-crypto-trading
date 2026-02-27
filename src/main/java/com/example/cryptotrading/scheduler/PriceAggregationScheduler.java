package com.example.cryptotrading.scheduler;

import com.example.cryptotrading.service.PriceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class PriceAggregationScheduler {

    private final PriceService priceService;

    @Scheduled(fixedRate = 10000)
    public void aggregatePrices() {
        log.info("Starting price aggregation");
        priceService.aggregatePrices();
    }
}
