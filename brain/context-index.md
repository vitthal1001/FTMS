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

Production foundations are implemented for the gateway, authentication, and account domains. They are not yet deployed to Kubernetes or GCP.

Foundation completed:

- Service directories created for all target services.
- Service ownership README files created.
- Shared Spring library `services/shared/spring-common` created with API exception, JWT principal contract, correlation ID filter, security headers filter, centralized exception handler, and unit tests.
- Frontend bootstrap created under `frontend` with React, TypeScript, Tailwind, Redux Toolkit, runtime config, and initial operations console shell.

Implemented services:

- `api-gateway`: Spring Cloud Gateway WebFlux service with auth routing, Redis rate limiting, JWT validation, correlation ID propagation, request logging, circuit-breaker fallback, actuator, Prometheus metrics, OpenAPI docs, Dockerfile, and Kubernetes base manifest.
- `auth-service`: Spring MVC/JPA service with registration, login, refresh-token rotation, logout, `/me`, BCrypt password hashing, PostgreSQL persistence, Redis token-family session markers, Kafka auth events, account lockout, validation, centralized exception handling, actuator, Prometheus metrics, OpenAPI docs, Dockerfile, Flyway migration, and Kubernetes base manifest.
- `account-service`: Spring MVC/JPA service with account creation/retrieval/status APIs, immutable ledger entries, balance snapshots, idempotency records, replay-safe transfers, JWT/RBAC enforcement, Kafka account events, validation, centralized exception handling, actuator, Prometheus metrics, OpenAPI docs, Dockerfile, Flyway migration, Testcontainers tests, and Kubernetes base manifest.

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
- Implement Helm charts for `api-gateway` and `auth-service`.
- Define initial user-service API contracts.
- Define platform-wide Kafka schema registry and DLQ standards.
- Add CI pipeline once build commands are verified locally.
- Add transactional outbox for auth event delivery hardening.
- Add transactional outbox for account event delivery hardening.
- Implement transaction-service saga orchestration on top of account transfer contracts.

## Known Issues

- Gradle wrapper is not present yet.
- Frontend dependencies are declared but not installed.
- Frontend lockfile generation was attempted but npm registry resolution hung without output and was stopped.
- Docker Compose images have not been pulled or started in this session.
- `auth-service` Kafka publishing is asynchronous and logs publish failures; transactional outbox is pending.
- `account-service` Kafka publishing is after-commit but not transactional-outbox-backed yet.
- Kubernetes YAML syntax validates, but `kubectl apply --dry-run=client` could not complete because no Kubernetes API server is reachable at the current context.

## Deployment Status

- Local infrastructure: configured, not started.
- Application services: `api-gateway`, `auth-service`, and `account-service` are implemented and container definitions are configured; images not built in this session yet.
- Kubernetes: base namespace, network policy, gateway deployment/service, auth deployment/service, and account deployment/service manifests exist.
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
- Auth service built from `services/auth-service/Dockerfile` on `8081`.
- API gateway built from `services/api-gateway/Dockerfile` on `8080`.
- Account service built from `services/account-service/Dockerfile` on `8082`.
- Kafka topic initialization creates `user-registered`, `user-login`, `auth-failed`, `token-refreshed`, `account-created`, `account-frozen`, `account-activated`, and `balance-updated`.

## APIs Implemented

Shared API/error foundation:

- `ApiException` for stable service error codes and HTTP statuses.
- `GlobalExceptionHandler` returning RFC 7807 `ProblemDetail` responses.
- `JwtPrincipal` shared authenticated principal representation.

Auth service:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me`

Gateway:

- Routes `/api/v1/auth/**` to `auth-service`.
- Routes `/api/v1/accounts/**` to `account-service`.
- Publicly permits register, login, and refresh.
- Requires JWT for other routed requests.

Account service:

- `POST /api/v1/accounts`
- `GET /api/v1/accounts/{accountId}`
- `POST /api/v1/accounts/{accountId}/freeze`
- `POST /api/v1/accounts/{accountId}/activate`
- `GET /api/v1/accounts/{accountId}/balances`
- `GET /api/v1/accounts/{accountId}/ledger`
- `POST /api/v1/accounts/transfers`

## Kafka Topics

Implemented Kafka topics:

- `user-registered`
- `user-login`
- `auth-failed`
- `token-refreshed`
- `account-created`
- `account-frozen`
- `account-activated`
- `balance-updated`

Planned topic groups:

- Customer lifecycle events.
- Account lifecycle events.
- Transaction command and state events.
- Payment provider events.
- Fraud decision events.
- Audit events.
- Analytics projection events.
- Dead letter topics per consumer group or topic family.

## Database Schemas

Implemented database schemas:

- `auth_users`
- `auth_user_roles`
- `auth_refresh_tokens`
- `accounts`
- `ledger_entries`
- `account_balance_snapshots`
- `idempotency_records`

Planned ownership:

- `user-service`: customer profile, KYC status, preferences.
- `transaction-service`: transaction commands, idempotency keys, saga state, outbox.
- `payment-service`: provider mappings, payment attempts, reconciliation state.
- `audit-service`: immutable audit event store.
- `analytics-service`: projections and aggregate read models.

## Open Bugs

- None recorded.

## Next Steps

1. Build service jars with `gradle :services:api-gateway:bootJar :services:auth-service:bootJar :services:account-service:bootJar`.
2. Start Docker Compose stack when the user is ready to pull/build images.
3. Implement Helm charts for gateway, auth service, and account service.
4. Implement transaction-service saga orchestration and account transfer integration contracts.
5. Add transactional outbox for guaranteed auth/account Kafka event publication.

## Validation Completed

- Repository structure inspected with project-scoped `find`.
- `git diff --check` passed.
- `scripts/engineering-watchdog.sh` passed `bash -n`.
- `docker compose -f platform/docker/docker-compose.yml --env-file .env.example config --quiet` passed.
- `gradle :services:shared:spring-common:test :services:api-gateway:compileJava :services:auth-service:compileJava` passed.
- `gradle :services:api-gateway:test` passed.
- `gradle :services:auth-service:test` passed with PostgreSQL and Redis Testcontainers.
- `gradle test :services:api-gateway:bootJar :services:auth-service:bootJar` passed.
- `gradle :services:account-service:test` passed with PostgreSQL Testcontainers.
- `gradle test :services:api-gateway:bootJar :services:auth-service:bootJar :services:account-service:bootJar` passed.
- `docker compose -f platform/docker/docker-compose.yml --env-file .env.example config --quiet` passed after adding account service.
- `terraform fmt -check -recursive platform/terraform` passed.
- Kubernetes base manifests, including `account-service.yaml`, passed YAML parsing via Ruby `YAML.load_stream`.
- `terraform fmt -check -recursive platform/terraform` passed.
- `docker compose -f platform/docker/docker-compose.yml --env-file .env.example config --quiet` passed after adding app services.
- Kubernetes manifests passed YAML parsing via Ruby `YAML.load_stream`.
- `kubectl apply --dry-run=client --validate=false -f platform/kubernetes/base` could not complete because no Kubernetes API server is reachable.
