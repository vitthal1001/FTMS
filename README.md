# NeoBankX

NeoBankX is a production-oriented cloud-native digital banking platform designed as a modular fintech monorepo. The platform targets independently deployable services, event-driven workflows, strong security boundaries, and first-class observability from the first commit.

## Architecture

- Frontend: React, TypeScript, Tailwind, Redux Toolkit.
- Backend: Java 21, Spring Boot 3.5.x, Spring Security, JWT, Spring Cloud patterns.
- Services: API gateway, auth, user, account, transaction, payment, notification, fraud, audit, and analytics.
- Data: PostgreSQL for transactional consistency, Redis for low-latency state, MongoDB for document-oriented read and audit workloads.
- Messaging: Kafka with outbox, dead letter topics, idempotent consumers, and saga coordination.
- Platform: Docker Compose for local infrastructure, Kubernetes and Helm for deployable services, Terraform for GCP/GKE infrastructure.
- Observability: Prometheus, Grafana, Loki, Jaeger, Spring Actuator, structured logs, and propagated correlation IDs.

## Repository Layout

```text
frontend/                 React banking console
services/                 Spring Boot services and shared backend libraries
platform/docker/          Local infrastructure composition
platform/kubernetes/      Kubernetes base manifests
platform/helm/            Helm chart foundations
platform/terraform/       GCP/GKE infrastructure foundations
platform/monitoring/      Prometheus, Grafana, Loki, and logging config
docs/                     Architecture, API, security, and operations docs
brain/                    Persistent engineering memory and decisions
scripts/                  Engineering automation and watchdog tooling
```

## Local Foundation

Start the local platform dependencies from the repository root:

```bash
docker compose -f platform/docker/docker-compose.yml --env-file .env.example up -d
```

The local stack exposes PostgreSQL, Redis, MongoDB, Kafka, Kafka UI, Prometheus, Grafana, Loki, and Jaeger. Services are not all implemented in phase 1; directories exist to preserve deployability boundaries and prevent early coupling.

## Engineering Contract

Every production service must include health checks, metrics, structured logging, OpenAPI documentation, validation, centralized exception handling, unit tests, integration tests, Docker packaging, and Kubernetes deployment metadata before it is considered complete. See [PROJECT_RULES.md](/Users/parthchoudhari/FTMS/PROJECT_RULES.md) and [AGENTS.md](/Users/parthchoudhari/FTMS/AGENTS.md).

## Current Status

Phase 1 foundation is initialized: monorepo structure, documentation, Docker Compose infrastructure, monitoring configuration, shared Spring Boot library, frontend bootstrap, and persistent `/brain` memory system.

