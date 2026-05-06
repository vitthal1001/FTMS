# 2026-05-06 Account Foundation

## Added

- Added `services:account-service` Gradle module.
- Added JWT-protected account lifecycle APIs.
- Added PostgreSQL/Flyway schema for `accounts`, `ledger_entries`, `account_balance_snapshots`, and `idempotency_records`.
- Added immutable ledger trigger protection.
- Added balanced opening and transfer posting foundation.
- Added optimistic versioning on accounts, balances, and idempotency records.
- Added Testcontainers integration tests for account creation, idempotency replay, transfer posting, frozen-account blocking, OpenAPI, and health.
- Added Dockerfile, Docker Compose service wiring, Kafka topic initialization, and Kubernetes base manifest.

## Validation

- Account service compile and tests passed.
- Gateway compile passed after adding account route.

## Known Gaps

- Kafka account events are after-commit but not yet transactional-outbox-backed.
- Cross-service transaction saga is not implemented yet.

