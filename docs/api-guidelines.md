# API Guidelines

## HTTP

- Use resource-oriented URLs and versioned API paths.
- Use idempotency keys for write endpoints that clients may retry.
- Return RFC 7807 problem details for errors.
- Propagate `X-Correlation-Id` on every request and response.
- Never expose stack traces, SQL errors, token material, or provider secrets.

## Error Codes

Error titles must be stable uppercase codes such as `VALIDATION_FAILED`, `DUPLICATE_REQUEST`, or `INSUFFICIENT_FUNDS`. Human-readable text belongs in `detail`.

## Pagination

Use cursor pagination for customer-visible transaction streams and offset pagination only for bounded administrative views.

