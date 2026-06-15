# 💳 Financial Transaction Management System (FTMS)

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-3.6-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white)](https://kafka.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![Jenkins](https://img.shields.io/badge/Jenkins-CI%2FCD-D24939?style=for-the-badge&logo=jenkins&logoColor=white)](https://www.jenkins.io/)

FTMS is a distributed, event-driven financial transaction processing system built using **Java 17**, **Spring Boot**, and **Apache Kafka**. It simulates core banking/fintech backend workflows including account credit/debit, transaction settlement, and auditing with strong consistency guarantees.

## 🏛️ System Architecture

The system is split into 4 independently deployable microservices coordinated via Kafka events:

```mermaid
graph TD
    Client[Client / Postman] -->|REST API| Gateway[API Gateway / Saga Orchestrator]
    
    subgraph Event Bus (Kafka)
        direction LR
        DebitTopic[transaction.debit.request]
        CreditTopic[transaction.credit.request]
        SettlementTopic[transaction.settlement.request]
        AuditTopic[transaction.audit.log]
        DLQTopic[transaction.error.dlq]
    end
    
    Gateway -->|Publish Event| DebitTopic
    Gateway -->|Publish Event| CreditTopic
    Gateway -->|Publish Event| SettlementTopic
    
    DebitTopic --> AccountService[Account Service]
    CreditTopic --> AccountService
    
    SettlementTopic --> SettlementService[Settlement Service]
    
    AccountService -->|Audit Log| AuditTopic
    SettlementService -->|Audit Log| AuditTopic
    
    AuditTopic --> AuditService[Audit & Reporting Service]
    
    %% Error Flow
    AccountService -.->|Failures| DLQTopic
    SettlementService -.->|Failures| DLQTopic
    DLQTopic -.-> Gateway
```

---

## ⚡ Key Architectural Patterns

### 1. Saga Orchestration Pattern
To process transactions across multiple microservices without distributed two-phase commit (2PC) locks, FTMS implements a **Saga Orchestrator** in the gateway layer:
- **Happy Path**: The Orchestrator publishes events to `debit.request`. Once successful, it triggers `credit.request`, followed by `settlement.request`.
- **Compensating Path (Rollback)**: If a step fails (e.g., target account is suspended during credit), the orchestrator catches the event and publishes compensating events to refund the debited amount, maintaining eventual consistency.

### 2. Idempotency Layer
To prevent duplicate processing from network retries or Kafka "at-least-once" delivery:
- Every transaction request must include a unique `X-Transaction-Id` header (UUID v4).
- Services check a `processed_transactions` table before running logic. If the ID exists, the duplicate event is ignored.

### 3. Fault Isolation & Dead Letter Queues (DLQ)
- Transient database errors trigger up to 3 retries with exponential backoff.
- Validation failures or unparseable payloads (Poison Pills) are immediately routed to the `transaction.error.dlq` topic for offline analysis, preventing partition blockages.

---

## 📁 Project Structure

```text
ftms-parent/
├── saga-orchestrator/       # Saga coordinator & REST Gateway
├── account-service/         # User accounts, balances, credit/debit
├── settlement-service/      # Netting, ledger postings, clearing
├── audit-service/           # Transaction logging & report generation
├── docker-compose.yml       # Kafka, Zookeeper, MySQL, Redis, and services
└── README.md
```

---

## 💾 Database Schema (Core Tables)

### Account Service DB
*   **`accounts`**: `id` (PK), `account_number` (Unique), `balance`, `status` (ACTIVE, FROZEN), `version` (Optimistic Locking).
*   **`processed_requests`**: `idempotency_key` (PK), `status` (IN_PROGRESS, COMPLETED), `response_payload`, `created_at`.

### Settlement Service DB
*   **`settlements`**: `id` (PK), `transaction_id` (Unique), `amount`, `status` (PENDING, SETTLED, FAILED), `settled_at`.

---

## 🚀 Getting Started

### Prerequisites
- Docker & Docker Compose
- JDK 17
- Maven 3.8+

### Running the System

1.  Clone the repository:
    ```bash
    git clone https://github.com/vitthal1001/FTMS.git
    cd FTMS
    ```
2.  Start the infrastructure (Kafka, Zookeeper, MySQL):
    ```bash
    docker-compose up -d --build
    ```
3.  Build the services:
    ```bash
    mvn clean install
    ```
4.  Run the individual Spring Boot applications or start them containerized via docker-compose.

---

## 🔌 Core API Endpoints

### Saga Orchestrator Gateway (`localhost:8080`)

| Endpoint | Method | Headers | Payload | Description |
|---|---|---|---|---|
| `/api/v1/transactions` | `POST` | `X-Transaction-Id: <UUID>` | `{ "sourceAccount": "123", "targetAccount": "456", "amount": 1500.00 }` | Initiates a Saga-coordinated transfer. |
| `/api/v1/transactions/{id}` | `GET` | None | None | Checks current Saga execution state. |

---

## 🛠️ Verification & Testing
- **Unit Tests**: Built using JUnit 5 and Mockito, testing isolation behaviors.
- **Integration Tests**: Uses `Testcontainers` to spin up lightweight Kafka and MySQL instances during build verification.
