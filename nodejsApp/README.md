# Node.js User Service - Production Ready

A production-ready Node.js microservice demonstrating modern DevOps practices, database resilience, full observability, and comprehensive testing.

## 🚀 Quick Start

```bash
# Clone and start the application
git clone <repository>
cd nodejsApp

# Start with Docker Compose
docker compose up -d

# Access the application
open http://localhost:3000
```

## 📋 What This Project Demonstrates

### 🏗️ **Production Architecture Patterns**
- **Database Resilience**: App continues running when MongoDB is unavailable
- **Graceful Degradation**: Clear error messages when services are down
- **Health Checks**: Comprehensive health monitoring for all services
- **Container Orchestration**: Multi-service Docker Compose setup

### 🔍 **Full Observability Stack**
- **Structured Logging**: JSON logs with correlation IDs
- **Prometheus Metrics**: Business and system metrics at `/metrics`
- **Health Monitoring**: Service status at `/health`
- **System Dashboard**: Complete system overview at `/dashboard`

### 🧪 **Comprehensive Testing**
- **Unit Tests**: 15 tests with 78%+ coverage (no hardcoded thresholds)
- **E2E Tests**: Cypress integration tests
- **Security Scanning**: npm audit + retire.js
- **Linting**: ESLint with standard configuration
- **Coverage Reports**: LCOV, HTML, and text formats for SonarQube integration

### 🔒 **DevSecOps Pipeline**
- **CI/CD Ready**: Complete pipeline script
- **Security First**: Vulnerability scanning
- **Quality Gates**: Coverage thresholds and linting
- **Docker Ready**: Multi-stage builds with security

## 🌐 API Endpoints

| Method | Endpoint | Description | Database Required |
|--------|----------|-------------|-------------------|
| GET | `/` | Registration form UI | No |
| GET | `/dashboard` | System info & API list | No |
| GET | `/health` | Health check | No |
| GET | `/metrics` | Prometheus metrics | No |
| POST | `/register` | Register new user | Yes |
| GET | `/users` | List all users | Yes |

## 🏃‍♂️ Usage Examples

### Start the Application
```bash
# Start all services
docker compose up -d

# View logs
docker compose logs -f app

# Check service status
docker compose ps
```

### Test Database Resilience
```bash
# Stop database to test resilience
docker compose stop mongo

# App continues running - test endpoints
curl http://localhost:3000/health
curl http://localhost:3000/dashboard

# Try database operations (graceful failures)
curl -X POST http://localhost:3000/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","email":"test@example.com"}'

# Restart database
docker compose start mongo
```

### API Testing
```bash
# Health check
curl http://localhost:3000/health | jq

# System dashboard
curl http://localhost:3000/dashboard | jq

# Register user
curl -X POST http://localhost:3000/register \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com"}' | jq

# Get users
curl http://localhost:3000/users | jq

# View metrics
curl http://localhost:3000/metrics | grep user_registrations
```

## 🛠️ Development

### Local Development
```bash
# Install dependencies
npm install

# Run tests
npm test
npm run test:coverage

# Run linting
npm run lint
npm run lint:fix

# Security scanning
npm run audit
npm run security:check

# Start development server
npm run dev
```

### Full CI/CD Pipeline
```bash
# Run complete pipeline
./scripts/pipeline.sh

# Pipeline includes:
# 1. Static Analysis (ESLint)
# 2. Security Scanning (npm audit, retire.js)
# 3. Unit Tests (Jest with coverage)
# 4. Integration Tests
# 5. Docker Build
# 6. E2E Tests (Cypress)
```

## 📊 Monitoring & Observability

### Metrics Available
- `http_requests_total` - HTTP request counter
- `http_request_duration_seconds` - Request duration histogram
- `user_registrations_total` - Business metric for registrations
- `database_connection_status` - Database connectivity (1=connected, 0=disconnected)

### Health Check Response
```json
{
  "status": "healthy",
  "timestamp": "2025-11-06T10:58:39.699Z",
  "database": "connected"
}
```

### Dashboard Information
- Service metadata and version
- System information (OS, Node.js version, memory, CPU)
- Database status and connection details
- Complete API endpoint listing

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐
│   Node.js App   │    │    MongoDB      │
│   Port: 3000    │◄──►│   Port: 27017   │
│                 │    │                 │
│ ┌─────────────┐ │    │ ┌─────────────┐ │
│ │ Express API │ │    │ │ User Data   │ │
│ │ Metrics     │ │    │ │ Collections │ │
│ │ Health      │ │    │ │             │ │
│ └─────────────┘ │    │ └─────────────┘ │
└─────────────────┘    └─────────────────┘
```

## 🔧 Configuration

### Environment Variables
```bash
MONGO_HOST=mongo              # MongoDB hostname
MONGO_USERNAME=admin          # MongoDB username
MONGO_PASSWORD=password123    # MongoDB password
MONGO_DB=userapp             # Database name
MONGO_PORT=27017             # MongoDB port
PORT=3000                    # Application port
NODE_ENV=production          # Environment
```

### Docker Compose Services
- **app**: Node.js application with health checks
- **mongo**: MongoDB 7 with persistent storage
- **volumes**: Persistent data storage for MongoDB

## 🧪 Testing Strategy

## 🧪 Testing Strategy

### Test Coverage
- **Unit Tests**: 15 tests covering all major functionality
- **Coverage**: 78%+ statements, 60%+ branches, 84%+ functions
- **E2E Tests**: Complete user journey testing
- **Security Tests**: Dependency vulnerability scanning
- **SonarQube Ready**: LCOV reports generated for quality gate analysis

### Test Categories
1. **API Endpoints**: All routes tested with various scenarios
2. **Database Integration**: Connection handling and error cases
3. **Metrics**: Prometheus metrics validation
4. **Error Handling**: Graceful failure scenarios
5. **UI Testing**: Form submission and navigation



## 🔒 Security Features

- **Dependency Scanning**: Automated vulnerability detection
- **Input Validation**: Request data validation
- **Error Handling**: No sensitive data in error responses
- **Health Checks**: Service availability monitoring
- **Container Security**: Non-root user in Docker

## 📈 Production Best Practices

### ✅ **Implemented Patterns**
- Database connection pooling and retry logic
- Structured logging with correlation
- Graceful shutdown handling
- Health check endpoints
- Metrics collection
- Error boundary patterns
- Container security hardening
- Multi-stage Docker builds
- Comprehensive testing strategy

### 🚀 **DevOps Ready**
- Docker containerization
- Docker Compose orchestration
- CI/CD pipeline automation
- Security scanning integration
- Code quality gates
- Monitoring and alerting ready

## 🛑 Troubleshooting

### Common Issues
```bash
# Check service status
docker compose ps

# View application logs
docker compose logs app

# View database logs
docker compose logs mongo

# Restart services
docker compose restart

# Clean restart
docker compose down && docker compose up -d
```

### Database Connection Issues
- Verify MongoDB is running: `docker compose ps mongo`
- Check connection string in environment variables
- Review application logs for connection errors
- Test database connectivity: `docker compose exec mongo mongosh`

## 📝 License

This project demonstrates production-ready Node.js development practices and is intended for educational and reference purposes.

---

**Built with**: Node.js, Express, MongoDB, Docker, Jest, Cypress, Prometheus, Winston
