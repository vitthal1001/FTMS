# Account Service Runbook

## Purpose

`account-service` owns account lifecycle, immutable ledger postings, balance snapshots, and replay-safe account write APIs.

## Local Startup

Build the service:

```bash
gradle :services:account-service:bootJar
```

Start through Compose:

```bash
docker compose -f platform/docker/docker-compose.yml --env-file .env.example up -d postgres kafka kafka-topic-init account-service
```

## Health

- Liveness: `/actuator/health/liveness`
- Readiness: `/actuator/health/readiness`
- Metrics: `/actuator/prometheus`
- OpenAPI: `/v3/api-docs`

## Operational Checks

- Confirm Flyway applied `V1__create_account_schema.sql`.
- Confirm `accounts` contains `NBX-SYSTEM-OPENING`.
- Confirm `ledger_entries` update/delete attempts fail.
- Confirm `idempotency_records` stores account creation and transfer replay keys.
- Confirm Kafka topics exist: `account-created`, `account-frozen`, `account-activated`, `balance-updated`.

## Failure Modes

- `INSUFFICIENT_FUNDS`: source available balance is below transfer amount.
- `ACCOUNT_NOT_ACTIVE`: source or target account is frozen.
- `IDEMPOTENCY_KEY_REUSED`: client reused a key with a different request body.
- `SYSTEM_ACCOUNT_MISSING`: migration or configured system account ID is wrong.
- `SYSTEM_CURRENCY_MISMATCH`: opening balance currency does not match the configured system offset account.

