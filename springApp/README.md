# Spring Boot User App - Production Ready Microservice

A production-ready Spring Boot microservice with comprehensive observability, testing, and DevSecOps pipeline.

## Features

- Spring Boot 3.2 with Java 17
- PostgreSQL database with JPA/Hibernate
- Full OpenTelemetry observability (SDK integration, no agent)
- Comprehensive testing (unit, integration, E2E)
- Static analysis (Checkstyle, PMD, SpotBugs)
- Security scanning (OWASP Dependency Check)
- Docker containerization with multi-stage build
- Production-ready logging and monitoring

## Quick Start

```bash
# Build and test
mvn clean verify

# Run with PostgreSQL
docker-compose up -d postgres
mvn spring-boot:run

# Run full pipeline
./scripts/pipeline.sh

# Run with Docker
docker-compose up -d
```

## Project Structure

```
├── src/
│   ├── main/java/com/example/userapp/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST and Web controllers
│   │   ├── entity/          # JPA entities
│   │   ├── repository/      # Data access layer
│   │   └── service/         # Business logic layer
│   └── test/java/           # Test classes
├── config/                  # Configuration files (checkstyle, etc.)
├── scripts/                 # Build and deployment scripts
└── reports/                 # Generated reports (timestamped)
```

## DevSecOps Pipeline

The `scripts/pipeline.sh` executes the following stages:

### 1. Clean & Build
```bash
mvn clean compile
```

### 2. Static Analysis
```bash
mvn checkstyle:check    # Code style validation
mvn spotbugs:check      # Bug pattern detection
mvn pmd:check          # Static code analysis
```

### 3. Security Scanning
```bash
mvn org.owasp:dependency-check-maven:check  # Vulnerability scanning
```

### 4. Testing & Coverage
```bash
mvn test               # Unit tests with JUnit 5 + Mockito
mvn jacoco:report      # Generate coverage report
mvn jacoco:check       # Coverage verification (≥80% required)
```

### 5. Integration Tests
```bash
mvn verify             # Integration tests with Testcontainers
```

### 6. Docker Build
```bash
mvn spring-boot:build-image  # Build Docker image
```

### 7. Report Collection
Reports are collected in `reports/<timestamp>/`:
- `dependency-check-report.html` - Security vulnerabilities
- `coverage/index.html` - Code coverage report
- `spotbugs.html` - SpotBugs analysis
- `pmd.html` - PMD analysis
- `checkstyle-result.xml` - Checkstyle report

## Observability

### OpenTelemetry Integration (SDK)
- **No Java agent required** - uses OpenTelemetry SDK directly
- Automatic Spring Boot instrumentation
- Trace correlation with logs using MDC

### Metrics Export
- **Prometheus metrics**: `http://localhost:8080/actuator/prometheus`
- Custom business metrics for user registrations
- Micrometer integration with Spring Boot Actuator

### Trace Export
- **OTLP traces**: Exported to `http://otel-collector:4317`
- Automatic Spring Boot instrumentation
- Trace correlation with logs

### Structured Logging
- **JSON format** with trace correlation
- Includes `traceId` and `spanId` in logs
- Different formats for local vs production

## Configuration

### Environment Variables
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/userapp
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password
MANAGEMENT_OTLP_TRACING_ENDPOINT=http://otel-collector:4317
```

### OpenTelemetry Data Flow
```
Application → OpenTelemetry SDK → OTLP Exporter → otel-collector:4317
                                ↓
                            Prometheus Metrics → /actuator/prometheus
                                ↓
                            JSON Logs → stdout (with traceId/spanId)
```

## API Endpoints

### REST API
- `POST /api/users` - Register new user
- `GET /api/users` - List all users
- `GET /api/users/{id}` - Get user by ID
- `DELETE /api/users/{id}` - Delete user
- `GET /api/users/stats` - User statistics

### Web UI
- `GET /` - User registration form
- `POST /register` - Submit registration form

### Actuator Endpoints
- `GET /actuator/health` - Health check
- `GET /actuator/prometheus` - Prometheus metrics
- `GET /actuator/info` - Application info

## Docker

### Multi-stage Build
```bash
# Build image
mvn spring-boot:build-image
# OR
docker build -t spring-user-app .
```

### Run with Dependencies
```bash
docker-compose up -d
```

The compose file includes:
- `springapp` - The Spring Boot application
- `postgres` - PostgreSQL database
- Health checks for both services
- Proper dependency ordering

## Testing Strategy

### Unit Tests (JUnit 5 + Mockito)
- Service layer testing with mocked dependencies
- Controller testing with MockMvc
- Repository testing with @DataJpaTest
- 80%+ code coverage requirement

### Integration Tests (Testcontainers)
- Full application context testing
- Real PostgreSQL database with Testcontainers
- End-to-end API testing
- Database integration validation

## Development

### Local Development
```bash
# Start database
docker-compose up -d postgres

# Run application
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Run tests
mvn test

# Run integration tests
mvn verify
```

## Database availability behavior

This application is resilient to the database being temporarily unreachable. Changes made:

- Hikari configuration (`spring.datasource.hikari.initialization-fail-timeout=-1`) prevents the app from failing to start if the database is down.
- A `DatabaseAvailability` component performs a quick connection check and is used by the service and controllers.
- The web UI (`/`) will display a banner when the database is down and operate in read-only mode (no registrations).
- The `/health` view reflects whether the database is reachable. Actuator `/actuator/health` will still include the datasource health details.

How to test:

1. Start the app without starting Postgres. The application should come up.
2. Visit `/actuator/health` to see the datasource reported as DOWN.
3. Visit `/health` (web view) or `/` to see the UI indicate database unavailability.
4. Start Postgres and verify the UI and actuator health change to UP.


### Pipeline Execution
```bash
# Full pipeline
./scripts/pipeline.sh

# Check reports
ls -la reports/$(ls reports/ | tail -1)/
```

## Configuration Files

All configuration files are organized in the `config/` directory:
- `config/checkstyle.xml` - Code style rules
- `config/checkstyle-suppressions.xml` - Style check suppressions
- `config/dependency-check-suppressions.xml` - Security scan suppressions
