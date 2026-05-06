# NeoBankX Helm

Helm charts will package each service independently. Shared chart conventions must include:

- Deployment, Service, ServiceMonitor, and PodDisruptionBudget.
- ConfigMap for non-secret config and Secret references for sensitive config.
- Readiness, liveness, and startup probes backed by Spring Actuator.
- Resource requests and limits for every container.
- OpenTelemetry, logging, and Prometheus labels.

Charts are intentionally not generated before service workloads exist. This prevents fake deployability and keeps Kubernetes contracts tied to real service ports, health paths, and dependencies.

