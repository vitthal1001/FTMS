variable "project_id" {
  description = "GCP project that will host NeoBankX development infrastructure."
  type        = string
}

variable "region" {
  description = "Primary GCP region for development infrastructure."
  type        = string
  default     = "us-central1"
}

