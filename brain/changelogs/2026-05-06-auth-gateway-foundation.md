# 2026-05-06 Auth and Gateway Foundation

## Added

- Added `services:api-gateway` Gradle module.
- Added Spring Cloud Gateway routing for `/api/v1/auth/**`.
- Added gateway JWT validation, Redis rate limiting, correlation IDs, request logging, fallback handling, Actuator, Prometheus, OpenAPI, Dockerfile, tests, and Kubernetes manifest.
- Added `services:auth-service` Gradle module.
- Added registration, login, refresh rotation, logout, and `/me` APIs.
- Added BCrypt password hashing, PostgreSQL persistence, Flyway migration, Redis token-family session markers, refresh-token replay protection, account lockout, Kafka event publishing, structured logging, Actuator, Prometheus, OpenAPI, Dockerfile, tests, and Kubernetes manifest.
- Added Docker Compose app services and Kafka topic initialization for auth events.
- Added auth API contract and token-boundary ADR.

## Validation

- Gradle tests and bootJar packaging passed.
- Auth integration tests passed with PostgreSQL and Redis Testcontainers.
- Docker Compose config validation passed.
- Terraform formatting check passed.
- Kubernetes YAML syntax validation passed.

## Known Gaps

- Auth event publishing is asynchronous; transactional outbox is still required for guaranteed event delivery.
- Kubernetes dry-run requires a reachable API server and was not completed in this session.

