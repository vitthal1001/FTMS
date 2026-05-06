# Shared Backend Libraries

Shared code is restricted to cross-cutting infrastructure that does not create domain coupling.

Current module:

- `spring-common`: exception handling, problem responses, correlation IDs, security headers, and shared authenticated principal shape.

Rules:

- Do not place account, transaction, payment, customer, fraud, audit, or analytics domain models here.
- Do not create generic repositories or service base classes.
- Add shared code only when at least two real services need the same infrastructure behavior.

