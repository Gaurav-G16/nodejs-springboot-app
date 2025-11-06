#!/bin/bash

set -e

SERVICE_NAME="nodejs-app"
BUILD_NUMBER=${BUILD_NUMBER:-$(date +%Y%m%d-%H%M%S)}
REPORTS_DIR="build-reports/${SERVICE_NAME}/${BUILD_NUMBER}"

echo "🚀 Starting simplified pipeline for ${SERVICE_NAME} - Build ${BUILD_NUMBER}"

# Create reports directory
mkdir -p ${REPORTS_DIR}

# 1. Install dependencies
echo "📦 Installing dependencies..."
npm ci

# 2. Linting
echo "🔍 Running ESLint..."
npm run lint
echo "✅ Linting passed"

# 3. Security scanning
echo "🔒 Running security scans..."
npm run report:dependency
cp reports/*.json ${REPORTS_DIR}/ 2>/dev/null || true
echo "✅ Security scan completed"

# 4. Unit tests with coverage
echo "🧪 Running unit tests with coverage..."
npm run test:coverage
cp coverage/lcov-report/index.html ${REPORTS_DIR}/coverage-report.html 2>/dev/null || true
echo "✅ Unit tests passed with coverage >= 75%"

# 5. Build Docker image
echo "🐳 Building Docker image..."
npm run build
echo "✅ Docker image built"

echo "🎉 Pipeline completed successfully!"
echo "📊 Reports available in: ${REPORTS_DIR}"
ls -la ${REPORTS_DIR}
