# account-service

Owns account products, balances, holds, status transitions, and ledger-facing account events.

Initial implementation must protect balance mutation through transactional boundaries and outbox publishing.

Implemented foundation:

- `POST /api/v1/accounts`
- `GET /api/v1/accounts/{accountId}`
- `POST /api/v1/accounts/{accountId}/freeze`
- `POST /api/v1/accounts/{accountId}/activate`
- `GET /api/v1/accounts/{accountId}/balances`
- `GET /api/v1/accounts/{accountId}/ledger`
- `POST /api/v1/accounts/transfers`

Banking controls:

- PostgreSQL transaction boundary for account creation and transfers.
- `BigDecimal` money handling with two-decimal validation.
- Immutable `ledger_entries` enforced by database triggers.
- Optimistic version columns on accounts, balance snapshots, and idempotency records.
- Replay-safe account creation and transfers through `Idempotency-Key`.
- Kafka events for account lifecycle and balance updates.
