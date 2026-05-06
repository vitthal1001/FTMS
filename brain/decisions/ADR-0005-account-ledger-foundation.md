# ADR-0005: Account Ledger and Balance Foundation

Date: 2026-05-06

## Status

Accepted

## Context

Account balances cannot be maintained as casual mutable counters. Account creation, transfers, status changes, balance retrieval, and replay-safe writes need one transactional consistency boundary.

## Decision

`account-service` owns accounts, immutable ledger entries, balance snapshots, and idempotency records in PostgreSQL. Opening balances and transfers are posted as balanced ledger groups where signed amounts sum to zero. Balance snapshots are updated in the same transaction as ledger entries and are protected by optimistic version columns.

The service uses `BigDecimal` with two-decimal validation for money values. Ledger rows are immutable at the database layer through update/delete rejection triggers.

## Consequences

- Balance reads are fast through `account_balance_snapshots`.
- Ledger history remains auditable and cannot be rewritten through normal database operations.
- Transfers are atomic inside one PostgreSQL transaction.
- Cross-service money movement still requires a future saga/outbox layer before external payments are introduced.
- Kafka publishing is after-commit but not yet outbox-backed, so guaranteed delivery remains a known follow-up.

