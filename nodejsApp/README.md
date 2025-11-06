# Node.js User App - Production Ready

A production-ready Node.js application with comprehensive observability, testing, and CI/CD pipeline.

## Features

- Express.js web server with MongoDB integration
- Full OpenTelemetry observability (traces, metrics, logs)
- Comprehensive testing (unit, integration, E2E)
- Security scanning and dependency checks
- Docker containerization
- Production-ready logging and monitoring

## Quick Start

```bash
# Install dependencies
npm ci

# Start development server
npm run dev

# Run full pipeline
npm run pipeline
```

## Pipeline Stages

### 1. Static Analysis
```bash
npm run lint          # ESLint with standard config
npm run lint:fix      # Auto-fix linting issues
```

### 2. Security Scanning
```bash
npm run audit         # npm audit for vulnerabilities
npm run security:check # retire.js for known vulnerabilities
npm run report:dependency # Generate security reports
```

### 3. Testing
```bash
npm test              # Unit tests with Jest
npm run test:coverage # Unit tests with coverage (>=80% required)
npm run test:e2e      # E2E tests with Cypress
```

### 4. Build & Deploy
```bash
npm run build         # Build Docker image
docker-compose up -d  # Start with dependencies
```

## Observability

### Metrics
- Prometheus metrics available at `/metrics`
- Custom business metrics for user registrations
- HTTP request metrics (duration, count, status codes)

### Tracing
- OpenTelemetry traces exported to Tempo/Jaeger
- Automatic Express.js and HTTP instrumentation
- Trace correlation with logs

### Logging
- Structured JSON logging with Winston
- Request/response logging
- Error tracking and correlation

## Local Development

### Start Observability Stack
```bash
# From project root
docker-compose -f docker-compose.observability.yml up -d
```

Access:
- Grafana: http://localhost:3000 (admin/admin)
- Prometheus: http://localhost:9090
- Tempo: http://localhost:3200

### Environment Variables
```bash
MONGO_USERNAME=admin
MONGO_PASSWORD=password123
MONGO_DB=userapp
MONGO_HOST=localhost
MONGO_PORT=27017
PORT=3000
OTEL_EXPORTER_OTLP_TRACES_ENDPOINT=http://localhost:4318/v1/traces
```

## API Endpoints

- `GET /` - Registration form
- `POST /register` - Register new user
- `GET /users` - List all users
- `GET /health` - Health check
- `GET /metrics` - Prometheus metrics

## Testing Strategy

### Unit Tests (Jest + Supertest)
- Controller logic testing
- Service layer testing
- Database interaction mocking
- 80%+ code coverage requirement

### E2E Tests (Cypress)
- Full user journey testing
- API endpoint validation
- UI interaction testing

## Reports

Pipeline generates reports in `build-reports/<service>/<build-number>/`:
- `npm-audit-report.json` - Security vulnerabilities
- `retire-report.json` - Known vulnerable dependencies
- `coverage-report.html` - Code coverage report

## Docker

```bash
# Build image
docker build -t simple-user-app .

# Run with dependencies
docker-compose up -d
```

## Architecture

```
src/
├── app.js          # Main application class
└── telemetry.js    # OpenTelemetry configuration

tests/
└── app.test.js     # Unit tests

cypress/
└── e2e/
    └── app.cy.js   # E2E tests

scripts/
└── pipeline.sh    # Full CI/CD pipeline
```
