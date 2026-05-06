# NeoBankX Context Index

Last updated: 2026-05-06

## Current Architecture

NeoBankX is initialized as a cloud-native fintech monorepo with independently deployable service boundaries, a React operations frontend, Spring Boot backend foundation, Docker Compose local infrastructure, Kubernetes/Terraform platform foundations, and Prometheus/Grafana/Loki/Jaeger observability.

Architecture baseline:

- Edge: `api-gateway` will own routing, JWT validation, rate limiting, and secure headers.
- Identity: `auth-service` will own authentication, refresh token rotation, and RBAC claims.
- Customer domain: `user-service` will own customer profile and KYC state.
- Banking core: `account-service`, `transaction-service`, and `payment-service` will own account state, transaction orchestration, and payment rails.
- Risk and compliance: `fraud-service` and `audit-service` will own risk decisions and immutable activity records.
- Insights: `analytics-service` will own event-driven read models and analytics projections.
- Messaging: Kafka is the asynchronous integration backbone.
- Data: PostgreSQL, Redis, and MongoDB are available locally through Docker Compose.
- Observability: Prometheus, Grafana, Loki, and Jaeger are configured for local telemetry.

## Services Completed

No production service is fully implemented yet.

Foundation completed:

- Service directories created for all target services.
- Service ownership README files created.
- Shared Spring library `services/shared/spring-common` created with API exception, JWT principal contract, correlation ID filter, security headers filter, centralized exception handler, and unit tests.
- Frontend bootstrap created under `frontend` with React, TypeScript, Tailwind, Redux Toolkit, runtime config, and initial operations console shell.

## Current Sprint

Phase 1: repository foundation and production engineering structure.

Completed in this sprint:

- Git repository initialized on `main`.
- Git remote `origin` set to `https://github.com/vitthal1001/FTMS.git`.
- Root documentation created: `README.md`, `AGENTS.md`, `PROJECT_RULES.md`.
- Local infrastructure Compose stack created.
- Monitoring configs created for Prometheus, Grafana, Loki, Promtail, and Jaeger.
- Kubernetes namespace and default deny ingress policy created.
- Terraform dev provider boundary created.
- Engineering watchdog script created.
- Persistent `/brain` folder structure created.
- Engineering standards documentation created.
- Service shared-library governance documented.

## Pending Tasks

- Add Gradle wrapper after confirming local Gradle availability or approved dependency bootstrap.
- Implement API gateway with Spring Cloud Gateway, Redis rate limiting, actuator, OpenAPI aggregation, Dockerfile, tests, and Kubernetes chart.
- Implement auth-service JWT login, refresh token rotation, RBAC claims, audit events, persistence, Dockerfile, tests, and OpenAPI.
- Define initial API contracts for auth and user services.
- Define Kafka topic naming, partitioning, retention, schema ownership, and DLQ standards.
- Add CI pipeline once build commands are verified locally.
- Add service Helm chart templates only when real service ports, probes, and config contracts exist.

## Known Issues

- No application service is deployable yet; phase 1 intentionally avoids fake service containers.
- Gradle wrapper is not present yet.
- Frontend dependencies are declared but not installed.
- Frontend lockfile generation was attempted but npm registry resolution hung without output and was stopped.
- Local `gradle` CLI is not installed, so backend tests were not executed in this session.
- Local `terraform` CLI is not installed, so Terraform formatting was not executed in this session.
- Docker Compose images have not been pulled or started in this session.

## Deployment Status

- Local infrastructure: configured, not started.
- Application services: not deployed.
- Kubernetes: base namespace and network policy only.
- Terraform: dev provider boundary only; no cloud resources declared or applied.
- GCP/GKE: not provisioned.

## Infrastructure Inventory

Local Docker Compose services:

- PostgreSQL `postgres:16-alpine` on `5432`.
- Redis `redis:7-alpine` on `6379`.
- MongoDB `mongo:7` on `27017`.
- Kafka `bitnami/kafka:3.9` on `29092`.
- Kafka UI `provectuslabs/kafka-ui:v0.7.2` on `8088`.
- Prometheus `prom/prometheus:v2.55.1` on `9090`.
- Grafana `grafana/grafana:11.4.0` on `3000`.
- Loki `grafana/loki:3.3.2` on `3100`.
- Promtail `grafana/promtail:3.3.2`.
- Jaeger `jaegertracing/all-in-one:1.62.0` on `16686`, `4317`, and `4318`.

## APIs Implemented

No customer or service APIs are implemented yet.

Shared API/error foundation:

- `ApiException` for stable service error codes and HTTP statuses.
- `GlobalExceptionHandler` returning RFC 7807 `ProblemDetail` responses.
- `JwtPrincipal` shared authenticated principal representation.

## Kafka Topics

No Kafka topics are implemented yet.

Planned topic groups:

- Identity lifecycle events.
- Customer lifecycle events.
- Account lifecycle events.
- Transaction command and state events.
- Payment provider events.
- Fraud decision events.
- Audit events.
- Analytics projection events.
- Dead letter topics per consumer group or topic family.

## Database Schemas

No database schemas are implemented yet.

Planned ownership:

- `auth-service`: credentials, refresh token families, login attempts.
- `user-service`: customer profile, KYC status, preferences.
- `account-service`: accounts, balances, holds, account lifecycle.
- `transaction-service`: transaction commands, idempotency keys, saga state, outbox.
- `payment-service`: provider mappings, payment attempts, reconciliation state.
- `audit-service`: immutable audit event store.
- `analytics-service`: projections and aggregate read models.

## Open Bugs

- None recorded.

## Next Steps

1. Verify Gradle build for `services/shared/spring-common`.
2. Verify frontend TypeScript build after dependency installation.
3. Start Docker Compose stack when the user is ready to pull images.
4. Implement `api-gateway` as the first deployable Spring service.
5. Implement `auth-service` contracts before user or banking workflows.

## Validation Completed

- Repository structure inspected with project-scoped `find`.
- `git diff --check` passed.
- `scripts/engineering-watchdog.sh` passed `bash -n`.
- `docker compose -f platform/docker/docker-compose.yml --env-file .env.example config --quiet` passed.
