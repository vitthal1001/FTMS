# NeoBankX Terraform

Terraform owns GCP and GKE infrastructure. Execution is gated:

- `terraform plan` is allowed after configuration review.
- `terraform apply` requires explicit user approval.
- Billing actions require explicit user approval.
- Cluster deletion requires explicit user approval.

Expected modules:

- Network and private subnets.
- GKE cluster and node pools.
- Cloud SQL PostgreSQL.
- Memorystore Redis.
- Secret Manager.
- Artifact Registry.
- Workload Identity.
- Managed observability integrations.

