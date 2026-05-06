# Local Stack Runbook

## Start

```bash
docker compose -f platform/docker/docker-compose.yml --env-file .env.example up -d
```

## Health Checks

- PostgreSQL: `pg_isready`.
- Redis: `redis-cli ping`.
- MongoDB: `db.adminCommand('ping')`.
- Kafka: list topics through `kafka-topics.sh`.
- Prometheus: `http://localhost:9090/-/healthy`.
- Grafana: `http://localhost:3000`.
- Jaeger: `http://localhost:16686`.

## Common Failures

- Port collision: stop the conflicting local service or change the Compose port.
- Image pull failure: retry after confirming Docker registry access.
- Kafka startup delay: wait for the health check before starting dependent services.

