# api-gateway

Owns edge routing, JWT validation, rate limiting, request correlation, secure headers, and coarse RBAC enforcement.

Initial implementation will use Spring Cloud Gateway with:

- Token validation and claim propagation.
- Per-client and per-route rate limits backed by Redis.
- Circuit breakers and timeout policies for every downstream route.
- Prometheus metrics and structured access logs.
- OpenAPI aggregation for customer-facing APIs.

