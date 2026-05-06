# NeoBankX Project Rules

NeoBankX is a production-grade fintech platform, not a tutorial codebase. Every change must improve the system toward operational correctness, security, maintainability, and deployability.

## Non-Negotiables

1. No toy code. Code must handle failures, validate inputs, emit logs, expose observability, and be testable.
2. No undocumented contracts. APIs, Kafka topics, schemas, and operational assumptions must be recorded.
3. No hidden coupling. Services must be independently deployable and own their data.
4. No silent operational risk. Risky commands require explicit approval before execution.
5. No repeated failed fixes. Three repeated failures trigger root cause analysis under `brain/debugging`.

## Architecture Decisions

- PostgreSQL is the source of truth for transactional banking data.
- Kafka is the integration backbone for asynchronous workflows.
- Redis is used for cache, rate-limit counters, and short-lived session/security state.
- MongoDB is reserved for document-centric reads, audit views, analytics projections, and notification payload history.
- Service-to-service calls must be authenticated, traced, timeout-bound, and observable.
- The shared Spring library may contain cross-cutting infrastructure only. Domain logic belongs in owning services.

## Required Service Capabilities

Each production service must include:

- Dockerfile and container health check.
- Spring Actuator health, readiness, liveness, and Prometheus metrics.
- OpenAPI documentation.
- Structured logging with correlation IDs.
- Validation and centralized exception handling.
- Unit tests and integration tests.
- Kubernetes and Helm deployment metadata.
- Runbook with startup, failure modes, and rollback notes.

## Security Baseline

- JWT access tokens and refresh token rotation.
- RBAC enforced at the gateway and service layers.
- Secure headers, input validation, CSRF protections where browser credentials are used, and XSS-safe rendering.
- Secrets must come from environment variables, Kubernetes Secrets, Secret Manager, or encrypted Terraform variables.
- Audit logging is required for identity, account, transaction, payment, and administrative events.

## Git Workflow

- Use conventional commits.
- Keep commits logically scoped.
- Do not commit generated secrets, local state, dependency caches, or Terraform state.

