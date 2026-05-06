# ADR-0002: Local Observability From Phase 1

Date: 2026-05-06

## Status

Accepted

## Context

Banking systems require fast diagnosis of failures, latency, retries, fraud decisions, payment provider behavior, and audit gaps. Observability cannot be retrofitted safely after services are built.

## Decision

Create local Prometheus, Grafana, Loki, Promtail, and Jaeger configuration in phase 1. Every service must expose metrics, structured logs, and trace propagation as part of its definition of done.

## Consequences

- Service templates must include Actuator and Micrometer.
- Local development has more infrastructure but catches production concerns earlier.
- Alerts will be added when service SLOs and real endpoints exist.

