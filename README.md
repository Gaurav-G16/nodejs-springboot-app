# DevSecOps Microservices Platform

Production-ready microservices with comprehensive CI/CD pipelines, security scanning, testing, and full observability stack.

## 🏗️ Architecture

```
simple-app/
├── nodejsApp/                          # Node.js Express + MongoDB microservice
│   ├── src/                           # Application source code
│   ├── tests/                         # Unit, integration, and E2E tests
│   ├── scripts/pipeline.sh            # DevSecOps pipeline
│   └── reports/                       # Generated reports
├── springApp/                          # Spring Boot + PostgreSQL microservice
│   ├── src/main/java/                 # Application source code
│   ├── src/test/java/                 # Unit and integration tests
│   ├── scripts/pipeline.sh            # DevSecOps pipeline
│   └── reports/                       # Generated reports
├── docker-compose.observability.yml   # Observability stack
├── otel-collector-config.yaml         # OpenTelemetry configuration
├── prometheus.yml                     # Prometheus configuration
├── tempo.yaml                         # Tempo tracing configuration
├── promtail.yml                       # Promtail log collection
└── pipeline.sh                        # Main pipeline orchestrator
```

## 🚀 Quick Start

### Prerequisites
- Node.js 18+
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Git

### Environment Setup
```bash
# Clone repository
git clone <repository-url>
cd simple-app

# Copy environment template
cp .env.example .env

# Edit .env with your configuration
nano .env
```

### Run Complete Pipeline
```bash
# Run full DevSecOps pipeline for both services
./pipeline.sh
```

### Run Individual Services
```bash
# Node.js service pipeline
cd nodejsApp && ./scripts/pipeline.sh

# Spring Boot service pipeline  
cd springApp && ./scripts/pipeline.sh
```

## 📊 Pipeline Stages

Each microservice includes comprehensive pipeline stages:

### 1. Static Analysis
- **Node.js**: ESLint with standard configuration
- **Spring Boot**: Checkstyle, PMD, SpotBugs

### 2. Security Scanning
- **Node.js**: npm audit + retire.js for vulnerability detection
- **Spring Boot**: OWASP Dependency Check with NVD integration
- **Reports**: JSON and HTML formats in `reports/security/`

### 3. Testing Strategy
- **Unit Tests**: Jest (Node.js) / JUnit 5 + Mockito (Spring Boot)
- **Integration Tests**: Supertest (Node.js) / Testcontainers (Spring Boot)
- **E2E Tests**: Cypress (Node.js) / API testing (Spring Boot)
- **Coverage**: ≥80% required, fail below threshold

### 4. Build & Containerization
- Multi-stage Docker builds for optimized images
- Automated image tagging with build numbers
- Health checks and proper signal handling

### 5. Observability Integration
- OpenTelemetry traces, metrics, and logs
- Prometheus metrics collection
- Structured JSON logging with trace correlation

## 🔍 Observability Stack

### Components
- **Grafana** (http://localhost:3000) - Dashboards and visualization (admin/admin)
- **Prometheus** (http://localhost:9090) - Metrics collection and storage
- **Tempo** (http://localhost:3200) - Distributed tracing backend
- **Loki** (http://localhost:3100) - Log aggregation and querying
- **OpenTelemetry Collector** - Telemetry data processing and routing

### Available Metrics
- HTTP request metrics (duration, count, status codes)
- Business metrics (user registrations, errors)
- System metrics (CPU, memory, GC)
- Custom application metrics

### Distributed Tracing
- End-to-end request tracing across services
- Database query tracing with Testcontainers
- HTTP client/server instrumentation
- Trace correlation with structured logs

### Centralized Logging
- JSON structured logging format
- Trace ID correlation for request tracking
- Log-based alerting capabilities
- Centralized collection with Promtail

## 🧪 Testing Strategy

### Node.js Microservice
```bash
# Unit tests with coverage
npm run test:coverage

# Integration tests
npm run test:integration

# E2E tests with Cypress
npm run test:e2e

# Security scanning
npm run security:scan
```

### Spring Boot Microservice
```bash
# Unit tests with JaCoCo coverage
mvn test jacoco:report

# Integration tests with Testcontainers
mvn verify -Dtest=*IntegrationTest

# Security scanning
mvn org.owasp:dependency-check-maven:check

# Static analysis
mvn checkstyle:check pmd:check spotbugs:check
```

## 📈 Generated Reports

Pipeline generates comprehensive reports in `reports/` directories:

### Node.js Reports
- `reports/coverage/index.html` - Code coverage analysis
- `reports/security/npm-audit.json` - npm security vulnerabilities
- `reports/security/retire-report.json` - Known vulnerable dependencies
- `reports/integration/` - Integration test results

### Spring Boot Reports
- `reports/coverage/index.html` - JaCoCo coverage report
- `reports/security/dependency-check-report.html` - OWASP security scan
- `target/site/spotbugs.html` - Bug pattern analysis
- `target/site/pmd.html` - Code quality analysis
- `target/site/checkstyle.html` - Code style compliance

## 🛠️ Development Commands

### Node.js Development
```bash
cd nodejsApp

# Install dependencies
npm ci

# Development server with hot reload
npm run dev

# Linting
npm run lint:fix

# Clean build artifacts
npm run clean
```

### Spring Boot Development
```bash
cd springApp

# Compile application
mvn clean compile

# Run application
mvn spring-boot:run

# Run specific test class
mvn test -Dtest=UserServiceTest

# Package without tests
mvn package -DskipTests
```

## 🔧 Configuration

### Environment Variables

#### Node.js Service
```bash
NODE_PORT=3000
MONGO_USERNAME=admin
MONGO_PASSWORD=password123
MONGO_DB=userapp
MONGO_HOST=localhost
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318
```

#### Spring Boot Service
```bash
SPRING_PORT=8080
POSTGRES_HOST=localhost
POSTGRES_DB=userapp
POSTGRES_USERNAME=postgres
POSTGRES_PASSWORD=password
MANAGEMENT_OTLP_TRACING_ENDPOINT=http://localhost:4318/v1/traces
```

### Pipeline Configuration
```bash
BUILD_NUMBER=local-$(date +%Y%m%d-%H%M%S)
COVERAGE_THRESHOLD=80
NVD_API_KEY=your-nvd-api-key
```

## 🚦 Pipeline Execution

### Local Development
```bash
# Full pipeline (both services)
./pipeline.sh

# Individual service pipelines
cd nodejsApp && ./scripts/pipeline.sh
cd springApp && ./scripts/pipeline.sh
```

### CI/CD Integration
The pipeline scripts are designed for CI/CD environments:
- Exit codes indicate success/failure
- Reports generated in standard locations
- Environment variables control behavior
- Docker images tagged with build numbers

## 🔒 Security Features

### Implemented Security Measures
- Dependency vulnerability scanning with fail thresholds
- Static security analysis (OWASP, retire.js)
- Container security best practices
- Secure configuration management
- Rate limiting and input validation
- Helmet.js security headers (Node.js)
- Spring Security integration (Spring Boot)

### Security Reports
- CVSS scoring with configurable fail thresholds
- Suppression files for false positives
- JSON and HTML report formats
- Integration with NVD database

## 🎯 Best Practices Implemented

### Code Quality
- Consistent formatting and linting
- Comprehensive static analysis
- High test coverage requirements (≥80%)
- SOLID principles and clean architecture

### DevOps
- Infrastructure as Code
- Automated quality gates
- Comprehensive reporting
- Environment parity
- Container optimization

### Observability
- Three pillars implementation (metrics, traces, logs)
- Correlation between telemetry data
- Business metrics tracking
- Performance monitoring
- Alerting capabilities

## 🔄 Continuous Improvement

This platform provides foundation for:
- Automated quality gates
- Performance regression detection
- Security vulnerability monitoring
- Operational insights and alerting
- Capacity planning and scaling decisions

## 🆘 Troubleshooting

### Common Issues

#### Port Conflicts
```bash
# Check port usage
netstat -tulpn | grep :3000
netstat -tulpn | grep :8080

# Stop conflicting services
docker-compose -f docker-compose.observability.yml down
```

#### Database Connection Issues
```bash
# Check database containers
docker ps | grep postgres
docker ps | grep mongo

# View container logs
docker logs <container-id>
```

#### Test Failures
```bash
# Run tests with verbose output
npm run test -- --verbose
mvn test -X

# Check test reports
open reports/coverage/index.html
```

#### Memory Issues
```bash
# Increase Node.js memory
export NODE_OPTIONS="--max-old-space-size=4096"

# Increase Maven memory
export MAVEN_OPTS="-Xmx2g -XX:MaxPermSize=256m"
```

### Support
- Check pipeline logs in `reports/` directories
- Review Docker container logs
- Verify environment variables in `.env`
- Ensure all prerequisites are installed
