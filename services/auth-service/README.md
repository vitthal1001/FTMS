# auth-service

Owns identity, credential verification, refresh token rotation, account lockout, MFA extension points, and RBAC claim issuance.

Initial implementation will include JWT issuance, hashed refresh token storage, audit events for authentication decisions, and explicit failure telemetry.

Implemented foundation:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me`

Security controls:

- BCrypt password hashing.
- JWT access tokens signed from runtime `JWT_SECRET`.
- Opaque refresh tokens persisted only as SHA-256 hashes.
- Refresh-token rotation with family replay protection.
- Account lockout after repeated failed logins.
- Kafka events for `user-registered`, `user-login`, `auth-failed`, and `token-refreshed`.
