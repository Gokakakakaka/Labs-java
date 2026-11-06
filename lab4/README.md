# Auction REST API — Lab 2 / Task 3

A Spring Boot REST API backed by MySQL/MariaDB using JDBC. The domain is a small auction system.

## Domain model

- `users` — sellers and bidders.
- `items` — goods offered by a user (`items.user_id -> users.id`).
- `lots` — auction listing for an item (`lots.item_id -> items.id`, one lot per item).
- `birds` — legacy table name retained from the starter project; each row represents a **bid** (`lot_id`, bidder `user_id`, `price`, `date`).
- `deals` — completed auction result; references the lot and winning bid.

Prices are integer units (for example, cents); `date` values use `YYYY-MM-DD`. Auction statuses are `OPEN`, `CLOSED`, `CANCELLED`.

## Setup

1. Install MySQL 8+ or a compatible MariaDB version.
2. Run `mysql -u root -p < sql/schema.sql` from this directory. The script creates `java_app`, tables, foreign keys, sample rows and a restricted application account.
3. Set environment variables if needed:
   - `DB_URL=jdbc:mysql://localhost:3306/java_app`
   - `DB_USER=auction_app`
   - `DB_PASSWORD=change_me`
4. **Change the sample database password** in `sql/schema.sql` and set the same value in `DB_PASSWORD` before sharing/deploying the project.
5. Run `mvn spring-boot:run`. The application currently connects on startup through `Database.connect()`.
6. Import `postman/auction-api.postman_collection.json` into Postman. The collection uses `http://localhost:8080` by default.

Do not commit real credentials or use the sample password on an exposed server.

## Existing CRUD endpoints

For each of `/users`, `/items`, `/lots`, `/birds`, `/deals`:

- `GET /resource` — list rows
- `GET /resource/{id}` — retrieve one row
- `POST /resource` — create
- `PATCH /resource/{id}` — update
- `DELETE /resource/{id}` — delete (foreign-key constraints may reject deletion when related rows exist)

The request fields follow the table columns. For example, a lot uses `item_id`, `price` (mapped by the existing controller to `pprice`), `step`, `date`, and `status`.

## Five multi-step business operations

All operations use JDBC transactions; database changes are committed together or rolled back on SQL errors.

1. `POST /business/publish-item` — validates the owner and creates an item plus an `OPEN` lot in one transaction. JSON: `{"user_id":1,"about":"Camera","cndn":"Used - good","starting_price":100,"step":"10","date":"2027-12-31"}`.
2. `POST /business/lots/{lotId}/bids` — only accepts a bid for an open lot, checks the user, requires the bid to exceed the current price, inserts the bid and updates the lot's current price atomically. JSON: `{"user_id":2,"price":130,"date":"2026-10-22"}`.
3. `POST /business/lots/{lotId}/close` — closes an open lot with at least one bid, creates a deal for the highest bid and sets the lot price to the winning price. A lot can only be closed once.
4. `POST /business/lots/{lotId}/cancel` — cancels an open lot only if it has no bids or deals.
5. `DELETE /business/bids/{bidId}` — withdraws a bid only while its lot remains open, then recalculates the lot price from the remaining bids.

The current implementation returns a plain-text result string for both success and business-rule rejection. For production use, map these outcomes to structured JSON and HTTP status codes.

## Consistency and constraints

Foreign keys prevent orphan rows; unique email prevents duplicate accounts; one lot per item is enforced; checks constrain prices and status values. Multi-table business operations use transactions and row locks where concurrent updates matter. `birds` is named historically in the starter project but represents bids.

## Test scenarios

- Publish an item and lot, then query `/items` and `/lots`.
- Submit a bid below or equal to the current price: it should be rejected without inserting a bid.
- Submit a higher bid and verify both `/birds` and `/lots/{id}`.
- Close the lot and verify a row appears in `/deals` and the lot becomes `CLOSED`.
- Try closing the same lot again: it should be rejected.
- Cancel a fresh lot with no bids; then try bidding on it and verify rejection.
- Withdraw a bid while the lot is open and verify that the current price is recalculated.


## Задание 4 — безопасность

Реализация Spring Security, HTTP Basic, CSRF, регистрации с BCrypt, ролей USER/ADMIN и правил доступа описана в [README_SECURITY.md](README_SECURITY.md). Для новой базы используйте обновлённый `sql/schema.sql`; для существующей — после резервного копирования однократно выполните `sql/migration_security.sql`. Postman-коллекция оставлена без изменений.
