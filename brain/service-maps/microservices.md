# Microservice Map

| Service | Primary Responsibility | Initial Data Store | Event Role |
| --- | --- | --- | --- |
| api-gateway | Edge security and routing | Redis for rate limiting | None initially |
| auth-service | Identity and tokens | PostgreSQL + Redis | Publishes identity events |
| user-service | Customer profile and KYC | PostgreSQL | Publishes customer events |
| account-service | Accounts and balances | PostgreSQL | Publishes account events |
| transaction-service | Transaction orchestration | PostgreSQL | Publishes transaction events |
| payment-service | External payment rails | PostgreSQL | Publishes payment events |
| notification-service | Customer messaging | MongoDB | Consumes domain events |
| fraud-service | Risk decisions | PostgreSQL + Redis | Consumes transaction events |
| audit-service | Immutable audit log | MongoDB | Consumes and stores audit events |
| analytics-service | Read models and insights | MongoDB | Consumes platform events |

