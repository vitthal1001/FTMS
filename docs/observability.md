# Observability Foundation

## Signals

- Metrics: Spring Actuator and Micrometer expose `/actuator/prometheus`.
- Logs: services emit structured logs with `correlationId`, service name, event name, and outcome.
- Traces: OpenTelemetry exports to Jaeger locally and managed tracing in GCP.
- Health: every service must expose liveness, readiness, and dependency-aware health groups.

## Local Stack

- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000
- Loki: http://localhost:3100
- Jaeger: http://localhost:16686
- Kafka UI: http://localhost:8088

## Alerting Direction

Phase 1 does not create paging alerts. Service phases must add SLO-backed alerts for latency, error rate, consumer lag, failed outbox publishing, and dead letter topic growth.

