# Crypto Trading System

A Spring Boot application that simulates a crypto trading platform. Users can view aggregated prices, execute trades (BUY/SELL), and manage wallet balances. Price data is aggregated from Binance and Huobi at a fixed interval.

## Project Purpose

- **Price aggregation** – Fetches best bid/ask from Binance and Huobi every 10 seconds and stores the best prices in the database
- **Trading** – Execute BUY/SELL orders for supported pairs (BTCUSDT, ETHUSDT) at the latest aggregated price
- **Wallet** – View crypto wallet balances (BTC, ETH, USDT)
- **Trade history** – View past trades with pagination

## Tech Stack

- **Java 21**, **Spring Boot 3.5.0**, **Gradle**
- **H2** in-memory database
- **Spring Data JPA**, **Bean Validation**, **Lombok**

## Project Structure

```
src/main/java/com/example/cryptotrading/
├── CryptoTradingApplication.java
├── config/           # Scheduling, RestTemplate, app config
├── client/           # BinanceClient, HuobiClient (external API clients)
├── controller/       # PriceController, TradeController, WalletController
├── domain/           # OrderSideCodeEnum
├── dto/              # Request/response DTOs, GenericPage, PaginationRequest
├── entity/           # JPA entities (Trade, Wallet, Currency, etc.)
├── exception/        # GlobalExceptionHandler, custom exceptions
├── repository/       # JPA repositories
├── scheduler/        # PriceAggregationScheduler (10s interval)
├── service/          # TradeService, PriceService, WalletService
└── util/             # AmountFormatUtil, UserUtil
```

## Prerequisites

- **JDK 21**
- **Gradle** (wrapper included)

## How to Start

```bash
# From project root
./gradlew bootRun
```

The application runs on **http://localhost:8080**.

- Scheduler runs every 10 seconds to aggregate prices from Binance and Huobi
- To disable the scheduler (e.g. for tests), set `spring.scheduling.enabled=false`

## How to Run Tests

```bash
./gradlew test
```

To run without daemon (useful in CI):

```bash
./gradlew test --no-daemon
```

To run a specific test class:

```bash
./gradlew test --tests "com.example.cryptotrading.service.TradeServiceTest"
./gradlew test --tests "com.example.cryptotrading.controller.TradeControllerIntegrationTest"
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/prices` | Latest best aggregated prices (BTCUSDT, ETHUSDT) |
| POST | `/api/trades` | Execute a BUY or SELL trade |
| GET | `/api/trades` | Trade history (paginated, see query params below) |
| GET | `/api/wallets` | User's wallet balances |

### Trade History Pagination

`GET /api/trades` returns a `GenericPage`:

- `data` – list of trade records
- `totalCount` – total number of trades
- `pageNumber` – current page (0-based)
- `pageSize` – page size

Query parameters:

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `pageNumber` | int | 0 | Page index (0-based) |
| `pageSize` | int | 10000 | Items per page |

Example: `GET /api/trades?pageNumber=0&pageSize=10`

### Trade Request Example

```json
{
  "symbol": "BTCUSDT",
  "side": "BUY",
  "quantity": 0.5
}
```

Supported `side`: `BUY`, `SELL`.

## Postman Collection

A Postman collection with all scenarios (happy path and error cases) is available at:

**`postman/Crypto-Trading-API.postman_collection.json`**

Import it in Postman via **File → Import** and select this file.

The collection includes:

- **Prices** – Get latest aggregated prices
- **Wallets** – Get wallet balances
- **Trades – Happy path** – Buy BTCUSDT, Buy ETHUSDT, Sell BTCUSDT, Get trade history (default and paginated)
- **Trades – Error cases** – Invalid symbol, invalid side (HOLD), insufficient balance, negative quantity, missing fields, empty body

Collection variable: `baseUrl` defaults to `http://localhost:8080`.

## Initial Data

- **Demo user** (id=1) with initial balance **50,000 USDT**
- **Supported pairs**: BTCUSDT, ETHUSDT
- **Currencies**: BTC, ETH, USDT
- **Order sides**: BUY, SELL

## H2 Console

When the app is running, the H2 web console is available at:

**http://localhost:8080/h2-console**

Connection settings:

| Setting | Value |
|---------|-------|
| JDBC URL | `jdbc:h2:mem:cryptodb` |
| Username | `sa` |
| Password | *(leave empty)* |
| Driver Class | `org.h2.Driver` |

Data is in-memory and is lost when the application stops.

## Assumptions

- User is already authenticated; API uses a default user (id=1)
- No integration with external trading systems; execution is simulated using aggregated prices
- Bid price is used for SELL orders, Ask price for BUY orders

