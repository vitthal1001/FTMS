# ADR-0001: Monorepo With Independent Service Boundaries

Date: 2026-05-06

## Status

Accepted

## Context

NeoBankX needs multiple services, shared platform tooling, and consistent engineering standards. Early development benefits from atomic cross-service changes, but service coupling must stay controlled.

## Decision

Use a monorepo with one directory per service and keep services independently deployable. Shared code is limited to cross-cutting infrastructure such as error handling, correlation IDs, security headers, and common principal contracts.

## Consequences

- Consistent tooling and docs are easier to maintain.
- Service boundaries remain visible in the repository.
- Shared domain models are prohibited because they create hidden coupling.
- CI must eventually support service-scoped builds.

