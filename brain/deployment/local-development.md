# Local Development

Start local infrastructure:

```bash
docker compose -f platform/docker/docker-compose.yml --env-file .env.example up -d
```

Stop local infrastructure:

```bash
docker compose -f platform/docker/docker-compose.yml down
```

Do not remove volumes unless explicitly requested and impact is explained.

Service ports will be assigned as services are implemented:

- `8080`: API gateway.
- `8081`: auth-service.
- `8082`: user-service.
- `8083`: account-service.

