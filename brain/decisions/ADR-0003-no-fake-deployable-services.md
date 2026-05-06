# ADR-0003: Do Not Generate Fake Deployable Services

Date: 2026-05-06

## Status

Accepted

## Context

The target platform has ten services. Generating empty Spring Boot applications, Dockerfiles, and Helm charts for every service would create a misleading sense of production readiness.

## Decision

In phase 1, create service ownership directories and documentation, plus shared backend infrastructure. Add service applications, Dockerfiles, tests, and deployment manifests only when each service has real contracts, ports, health checks, and configuration.

## Consequences

- The repository is honest about deployment status.
- Future service implementation is slower but safer.
- `brain/context-index.md` must clearly mark services as not yet deployable.

