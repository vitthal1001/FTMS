# System Overview

NeoBankX is organized around domain-owned services with explicit contracts. The platform avoids shared databases and avoids domain-heavy shared libraries.

Key decisions:

- Use Kafka for asynchronous state propagation and workflow events.
- Use PostgreSQL for transactional domains that require consistency.
- Use Redis for short-lived operational state such as rate limits, cache entries, and token/session metadata.
- Use MongoDB for document-oriented projections and audit/query use cases where it fits access patterns.
- Keep Kubernetes and Helm configuration tied to real services instead of generating fake deployable manifests.

