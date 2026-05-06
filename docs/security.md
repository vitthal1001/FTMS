# Security Foundation

## Identity and Access

- Access tokens are JWTs validated at the gateway and revalidated for sensitive service actions.
- Refresh tokens must be rotated and stored as hashed token identifiers.
- RBAC claims must be explicit and narrow.
- Administrative and money movement operations require audit events.

## API Protection

- Validate all request DTOs with Jakarta Validation or equivalent frontend validation.
- Reject unknown sensitive fields at service boundaries.
- Use parameterized persistence APIs and migrations for schema changes.
- Use CSRF protections where browser credentials or cookies are used.
- Return RFC 7807 problem details for API errors without leaking internals.

## Secrets

- Local secrets live only in `.env` files excluded from git.
- Cloud secrets must use Secret Manager or Kubernetes Secrets integrated with Workload Identity.
- Terraform state must never contain plaintext credentials beyond approved provider-managed references.

## Auditability

Audit logs must include actor, action, target, timestamp, correlation ID, request source, and outcome. Sensitive values must be redacted.

