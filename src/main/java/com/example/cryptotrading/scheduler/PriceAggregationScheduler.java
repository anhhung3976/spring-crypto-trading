package com.example.cryptotrading.scheduler;

import com.example.cryptotrading.service.PriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PriceAggregationScheduler {

    private static final Logger log = LoggerFactory.getLogger(PriceAggregationScheduler.class);

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
