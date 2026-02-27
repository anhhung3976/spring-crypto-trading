# Crypto Trading System

Spring Boot application for crypto trading with price aggregation from Binance and Huobi.

## Tech Stack
- Java 21, Spring Boot 3.4, Gradle
- H2 in-memory database
- Spring Data JPA, Bean Validation

## Quick Start

```bash
./gradlew bootRun
```

The application starts on `http://localhost:8080`. H2 console is at `http://localhost:8080/h2-console`.

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/prices` | Latest best aggregated prices for BTCUSDT and ETHUSDT |
| POST | `/api/trades` | Execute a BUY or SELL trade |
| GET | `/api/trades` | User's trading history |
| GET | `/api/wallets` | User's wallet balances |

### Trade Request Example

```json
{
  "symbol": "BTCUSDT",
  "side": "BUY",
  "quantity": 0.5
}
```

## Initial Data
- Default user with 50,000 USDT balance
- Supported pairs: BTCUSDT, ETHUSDT
- Price aggregation runs every 10 seconds


## Accessing H2 in this project
1. Start the app
   ./gradlew bootRun or start debug mode in your IDE
2. Open the H2 console in your browser
   Go to:
   http://localhost:8080/h2-console
3. Use these connection settings
   In the H2 login page:
   JDBC URL: jdbc:h2:mem:cryptodb
   Username: sa
   Password: (leave empty)
   Driver Class: org.h2.Driver (usually auto-filled)
   Click Connect and you’ll see the USERS, WALLETS, AGGREGATED_PRICES, TRADES tables. Data lives only while the Spring Boot app is running (in‑memory DB).