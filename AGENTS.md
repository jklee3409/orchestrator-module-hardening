# AI Coding Agent Guidelines for Orchestrator Module

## Project Overview
This is a Spring Boot microservice for the "Datcha" mobile data trading platform. It handles data transactions (normal sales and auctions), Pay payments, user management, real-time notifications, and search functionality.

## Architecture & Data Flow
- **Core Components**: `transaction_feed` (auctions/bids), `pay` (payments), `alarm` (notifications), `user` (auth), `market_statistics`, `event` (quizzes)
- **Concurrency Control**: Redis Lua scripts for bid auctions (see `src/main/resources/scripts/bid.lua` and `bid_rollback.lua`)
- **Event-Driven**: `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` for post-transaction processing (e.g., `BidSucceededEventHandler`)
- **Search**: Elasticsearch integration for transaction feed queries
- **Notifications**: Kafka-based alarm system with SSE support
- **Payments**: Toss Payments integration for Pay charging/purchasing

## Key Patterns & Conventions
- **Type Managers**: Use `StatusManager`, `SalesTypeManager` for cached enum-like entities (e.g., `statusManager.getStatus("FEED", "ON_SALE")`)
- **Validation**: Custom exceptions in `common.exception` package with error codes
- **Redis Keys**: Structured as `bids:{feedId}:highest_price`, `bids:{feedId}:highest_bidder_id`, `bids:{feedId}:state_version`
- **Bid Logic**: Lua script handles atomic bid updates; Java code validates preconditions and finalizes DB state with rollback on failure
- **Event Publishing**: Use `ApplicationEventPublisher` for decoupling (e.g., `applicationEventPublisher.publishEvent(BidSucceededEvent.of(...))`)
- **QueryDSL**: Generated Q-classes in `build/generated/` for complex queries
- **DTOs**: Separate request/response DTOs in `dto.request` and `dto.response` subpackages

## Developer Workflows
- **Build**: `./gradlew build` (includes QueryDSL generation)
- **Test**: `./gradlew test` (uses Testcontainers for MySQL integration tests)
- **Debug Bids**: Check Redis keys and Lua script logs; DB state via `BidsRepository.findTopByTransactionFeedOrderByBidAmountDescBidTimeDesc`
- **Elasticsearch Sync**: `BidSucceededEventHandler` updates `TransactionFeedDocument.currentHighestPrice` after commits
- **Notifications**: Kafka producer sends `AlarmCreationDto` to `notification-group` topic

## Integration Points
- **External APIs**: Toss Payments (`payment.toss.*`), OAuth (Kakao/Google/Naver), Gmail SMTP
- **Databases**: MySQL (JPA), Redis (session/cache/bids), Elasticsearch (search)
- **Messaging**: Kafka for alarms, Redis Pub/Sub for SSE notifications
- **Deployment**: AWS EC2/RDS/S3, appspec.yml for CodeDeploy

## Examples
- **Bid Placement**: `BidServiceImpl.placeBid()` executes Lua script, then `finalizeBidAgainstCommittedState()` with DB lock and refund logic
- **Status Checks**: `validateBidPrecondition()` uses `statusManager.getStatus("FEED", "ON_SALE")` and `salesTypeManager.getBidSaleType()`
- **Event Handling**: `BidSucceededEventHandler` runs after commit to update Elasticsearch and send Kafka notifications to all bid participants</content>
<parameter name="filePath">E:\dev_factory\side-project\orchestrator-module-hardening\AGENTS.md
