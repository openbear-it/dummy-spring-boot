# dummy-spring-boot

Minimal Spring Boot service designed for observability testing. Exposes Actuator and Prometheus metrics; everything outside `/actuator` returns synthetic responses with random delays and occasional errors.

## Endpoints

| Path | Description |
|------|-------------|
| `/actuator/health` | Health check |
| `/actuator/prometheus` | Prometheus metrics scrape |
| `/**` | Dummy traffic — random 2xx/5xx, random delay |

## Build & run

```bash
# Run locally
./mvnw spring-boot:run

# Run tests
./mvnw verify

# Build JAR
./mvnw -B package -DskipTests
```

## Docker

```bash
# Build
docker build -t dummy-spring-boot .

# Run
docker run -p 8080:8080 dummy-spring-boot
```

The image is also published to `ghcr.io` on every push to `main` and on version tags (`v*`), for both `linux/amd64` and `linux/arm64`.

## Configuration

Key properties in `src/main/resources/application.yml`:

- `dummy.traffic.delay` — min/max response delay (ms)
- `dummy.traffic.weights` — relative weights for HTTP status codes
- Actuator and Prometheus are enabled via standard Spring Boot configuration

Logs are JSON-structured (Logstash encoder) for ELK ingestion.
