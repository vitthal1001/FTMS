# Account Service API Contract

Base path: `/api/v1/accounts`

All endpoints require a Bearer JWT. Roles are read from the JWT `roles` claim.

## Endpoints

| Method | Path | Roles | Purpose |
| --- | --- | --- | --- |
| POST | `/` | `CUSTOMER`, `ADMIN` | Create an account with optional opening balance |
| GET | `/{accountId}` | `CUSTOMER`, `SUPPORT`, `ADMIN` | Retrieve account metadata |
| POST | `/{accountId}/freeze` | `SUPPORT`, `ADMIN` | Freeze account debit/credit activity |
| POST | `/{accountId}/activate` | `SUPPORT`, `ADMIN` | Reactivate a frozen account |
| GET | `/{accountId}/balances` | `CUSTOMER`, `SUPPORT`, `ADMIN` | Retrieve ledger and available balance snapshot |
| GET | `/{accountId}/ledger` | `CUSTOMER`, `SUPPORT`, `ADMIN` | Retrieve immutable ledger entries |
| POST | `/transfers` | `CUSTOMER`, `ADMIN` | Atomically transfer funds between accounts |

## Idempotency

The following endpoints require `Idempotency-Key`:

- `POST /api/v1/accounts`
- `POST /api/v1/accounts/transfers`

The service stores a SHA-256 request hash and response body in `idempotency_records`. Replaying the same key with the same request returns the original response. Reusing the same key with a different request returns `IDEMPOTENCY_KEY_REUSED`.

## Money Rules

- Money is represented with `BigDecimal`.
- Amounts support at most two decimal places.
- Transfers must be positive.
- Account opening balances must be non-negative.
- Transfers are rejected if either account is frozen or the source has insufficient available balance.

## Ledger Rules

- `ledger_entries` are immutable; database triggers reject updates and deletes.
- Every posting group must balance to zero before persistence.
- Balance snapshots are updated in the same transaction as ledger entry insertion.
- Account and balance rows use optimistic version columns.

## Kafka Events

Published topics:

- `account-created`
- `account-frozen`
- `account-activated`
- `balance-updated`

Events are published after transaction commit by the service event publisher. A transactional outbox is still planned for guaranteed event delivery.

