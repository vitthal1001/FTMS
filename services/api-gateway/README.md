# api-gateway

Owns edge routing, JWT validation, rate limiting, request correlation, secure headers, and coarse RBAC enforcement.

Initial implementation will use Spring Cloud Gateway with:

- Token validation and claim propagation.
- Per-client and per-route rate limits backed by Redis.
- Circuit breakers and timeout policies for every downstream route.
- Prometheus metrics and structured access logs.
- OpenAPI aggregation for customer-facing APIs.

Implemented foundation:

- Routes `/api/v1/auth/**` to `auth-service`.
- Routes `/api/v1/accounts/**` to `account-service`.
- Permits public register, login, and refresh paths while requiring JWTs for other routes.
- Propagates `X-Correlation-Id` and authenticated identity headers.
- Uses Redis-backed rate limiting and circuit-breaker fallback.
