# ADR-0004: Auth Token and Gateway Boundary

Date: 2026-05-06

## Status

Accepted

## Context

NeoBankX needs secure authentication without coupling downstream services to credential storage or refresh-token internals.

## Decision

`auth-service` owns credentials, BCrypt password hashing, refresh-token families, token rotation, replay detection, lockout state, and auth events. `api-gateway` validates access JWTs at the edge, rate-limits requests using Redis, forwards identity headers only after successful JWT validation, and routes auth endpoints to `auth-service`.

Refresh tokens are opaque random values. Only SHA-256 hashes are stored in PostgreSQL. Token-family activity is mirrored in Redis for session visibility and low-latency revocation markers.

## Consequences

- Downstream services trust gateway-propagated identity only inside the service mesh boundary.
- Refresh-token replay can revoke an entire family without exposing token material.
- JWT signing secret must be managed through environment, Kubernetes Secrets, or cloud Secret Manager.
- Auth events are asynchronous and currently logged on publish failure; future transactional outbox work should harden event delivery.

