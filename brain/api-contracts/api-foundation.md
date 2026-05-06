# API Foundation

Current implemented API foundation:

- RFC 7807 `ProblemDetail` response handling through `GlobalExceptionHandler`.
- Stable uppercase error titles through `ApiException`.
- `X-Correlation-Id` request and response propagation through `CorrelationIdFilter`.

Pending contracts:

- Auth login and refresh APIs.
- Gateway route and RBAC contract.
- User profile APIs.
- Idempotency header contract for write APIs.
- Pagination and filtering contract.

