# Microservice Map

| Service | Primary Responsibility | Initial Data Store | Event Role |
| --- | --- | --- | --- |
| api-gateway | Edge security, routing, JWT validation, rate limiting | Redis for rate limiting | Routes auth APIs |
| auth-service | Identity, credentials, tokens, lockout, refresh rotation | PostgreSQL + Redis | Publishes auth events |
| user-service | Customer profile and KYC | PostgreSQL | Publishes customer events |
| account-service | Accounts and balances | PostgreSQL | Publishes account events |
| transaction-service | Transaction orchestration | PostgreSQL | Publishes transaction events |
| payment-service | External payment rails | PostgreSQL | Publishes payment events |
| notification-service | Customer messaging | MongoDB | Consumes domain events |
| fraud-service | Risk decisions | PostgreSQL + Redis | Consumes transaction events |
| audit-service | Immutable audit log | MongoDB | Consumes and stores audit events |
| analytics-service | Read models and insights | MongoDB | Consumes platform events |
