# Deployment Foundation

## Local

Local dependencies run through Docker Compose. Application services will be run directly during early development and containerized as each service reaches deployable status.

`api-gateway` and `auth-service` now have Dockerfiles and Compose service definitions. Build the service jars before starting app containers:

```bash
gradle :services:api-gateway:bootJar :services:auth-service:bootJar
docker compose -f platform/docker/docker-compose.yml --env-file .env.example up -d
```

For real environments, replace `JWT_SECRET` and database credentials with secret-manager-backed values.

## Kubernetes

Kubernetes manifests start with namespace and network policy guardrails. `api-gateway` and `auth-service` now include base deployment and service manifests with probe, resource, and secret-reference contracts.

The manifests intentionally reference Kubernetes Secrets rather than hardcoded credentials:

- `api-gateway-secrets`
- `auth-service-secrets`

## Terraform

Terraform is initialized with a development environment provider boundary. `terraform apply`, billing operations, and cluster deletion require explicit approval before execution.
