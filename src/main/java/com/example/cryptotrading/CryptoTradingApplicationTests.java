package com.example.cryptotrading;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "spring.scheduling.enabled=false")
class CryptoTradingApplicationTests {

    @Test
    void contextLoads() {
    }
}
