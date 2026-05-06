# Auth Service API Contract

Base path: `/api/v1/auth`

## Endpoints

| Method | Path | Auth | Purpose |
| --- | --- | --- | --- |
| POST | `/register` | Public | Create customer identity and issue initial token family |
| POST | `/login` | Public | Authenticate credentials and issue token family |
| POST | `/refresh` | Public | Rotate refresh token and issue a new access token |
| POST | `/logout` | Public | Revoke the supplied refresh token family |
| GET | `/me` | Bearer JWT | Return authenticated subject, email, and roles |

## Error Shape

Errors use RFC 7807 `ProblemDetail` responses through the shared exception handler.

Stable error titles currently include:

- `WEAK_PASSWORD`
- `EMAIL_ALREADY_REGISTERED`
- `AUTHENTICATION_FAILED`
- `REFRESH_TOKEN_EXPIRED`
- `REFRESH_TOKEN_REPLAYED`
- `VALIDATION_FAILED`

## Security Rules

- Passwords are hashed with BCrypt cost 12.
- Access tokens are HS256 JWTs signed from `JWT_SECRET`; the secret must be provided by runtime configuration.
- Refresh tokens are opaque random values and only SHA-256 hashes are persisted.
- Refresh token reuse revokes the token family and publishes `auth-failed`.
- Login failures increment an account lockout counter.

## Kafka Events

Published topics:

- `user-registered`
- `user-login`
- `auth-failed`
- `token-refreshed`

Event payload includes `eventId`, `type`, `subject`, `email`, `occurredAt`, `correlationId`, and metadata.

