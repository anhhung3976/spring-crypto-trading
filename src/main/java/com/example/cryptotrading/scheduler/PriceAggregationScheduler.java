package com.example.cryptotrading.scheduler;

import com.example.cryptotrading.service.PriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PriceAggregationScheduler {

    private final PriceService priceService;

    public PriceAggregationScheduler(PriceService priceService) {
        this.priceService = priceService;
    }

    @Scheduled(fixedRate = 10000)
    public void aggregatePrices() {
        log.debug("Starting price aggregation");
        priceService.aggregatePrices();
    }
}
