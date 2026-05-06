# Deployment Foundation

## Local

Local dependencies run through Docker Compose. Application services will be run directly during early development and containerized as each service reaches deployable status.

## Kubernetes

Kubernetes manifests start with namespace and network policy guardrails. Service manifests will be introduced per service when ports, probes, and config contracts are real.

## Terraform

Terraform is initialized with a development environment provider boundary. `terraform apply`, billing operations, and cluster deletion require explicit approval before execution.

