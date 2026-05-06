# NeoBankX Agent Instructions

These instructions apply to the entire repository.

## Operating Rules

- Inspect existing files before generating or changing code.
- Never run destructive commands such as `rm -rf`, `git reset --hard`, cluster deletion, Terraform apply, or billing actions without explicit user approval.
- Keep changes modular and consistent with the service boundary being modified.
- Update `brain/context-index.md` after every major task.
- Explain architecture decisions when introducing new structure, service boundaries, or platform conventions.
- If the same build, test, stack trace, or file-edit failure repeats three times, stop implementation and write a root cause note under `brain/debugging`.

## Engineering Standards

- Build production-grade software: validation, failure handling, logging, observability, tests, and deployment readiness are required.
- Prefer clean architecture, DDD vocabulary, and hexagonal boundaries where the service has non-trivial domain behavior.
- Keep services independently deployable. Shared code must be small, stable, and infrastructure-oriented rather than domain-heavy.
- Do not invent API contracts, database schemas, or Kafka topics without documenting them in `brain/context-index.md` and the relevant `brain/api-contracts` or `brain/service-maps` file.
- Use conventional commits when committing: `feat:`, `fix:`, `refactor:`, `infra:`, `security:`, `perf:`, `docs:`, or `test:`.

## Backend Requirements

- Java 21 and Spring Boot 3.5.x are the phase-1 backend baseline.
- Every service must expose Spring Actuator health and Prometheus metrics.
- Every service must propagate `X-Correlation-Id`.
- Every service must use centralized exception handling and structured JSON logs.
- External calls must use timeouts, retries, circuit breakers, and clear fallback behavior.
- Persistence code must use parameterized access through framework APIs, migrations, and validation.

## Frontend Requirements

- React, TypeScript, Tailwind, and Redux Toolkit are the phase-1 frontend baseline.
- UI code must be typed, accessible, responsive, and designed for real banking workflows.
- Keep API clients behind typed modules and avoid hardcoded production endpoints.

## Documentation Requirements

- Keep architecture diagrams, ADRs, changelogs, deployment docs, runbooks, API docs, and debugging notes current.
- Update `brain/context-index.md` whenever services, topics, schemas, APIs, infrastructure, bugs, or deployment status change.

