# Engineering Standards

## Code Quality

- Keep domain logic inside the owning service.
- Keep shared libraries infrastructure-oriented and stable.
- Use clear package boundaries: API, application, domain, infrastructure, and configuration.
- Prefer constructor injection and immutable request/response records.
- Keep configuration externalized through environment variables or typed config properties.

## Testing

- Unit tests cover domain rules and failure paths.
- Integration tests cover persistence, messaging, and security behavior with Testcontainers.
- Contract tests are required before another service depends on an API or event.
- API tests must include validation, authorization, idempotency, and error shape checks.

## Observability

- Every service emits structured logs with `correlationId`.
- Every service exposes `/actuator/health` and `/actuator/prometheus`.
- Every service propagates trace context for inbound requests, outbound requests, and Kafka messages.
- Business events must include enough identifiers for production debugging without leaking secrets.

## Resilience

- External calls must define timeout, retry, and circuit breaker behavior.
- Kafka consumers must be idempotent and dead-letter failures that cannot be retried safely.
- Write APIs that may be retried by clients must support idempotency keys.

## Security

- No hardcoded secrets.
- No direct string-built SQL.
- No stack traces in API responses.
- No unvalidated money movement commands.
- No unaudited privileged actions.

