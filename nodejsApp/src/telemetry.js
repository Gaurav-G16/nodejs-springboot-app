const { NodeSDK } = require('@opentelemetry/sdk-node')
const { Resource } = require('@opentelemetry/resources')
const { SemanticResourceAttributes } = require('@opentelemetry/semantic-conventions')
const { OTLPTraceExporter } = require('@opentelemetry/exporter-trace-otlp-http')
const { getNodeAutoInstrumentations } = require('@opentelemetry/auto-instrumentations-node')
const winston = require('winston')

// Create logger with structured format
const logger = winston.createLogger({
  level: 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.errors({ stack: true }),
    winston.format.json()
  ),
  transports: [
    new winston.transports.Console(),
    new winston.transports.File({ 
      filename: './logs/app.log',
      maxsize: 5242880, // 5MB
      maxFiles: 5
    })
  ]
})

// Create logs directory if it doesn't exist
const fs = require('fs')
if (!fs.existsSync('./logs')) {
  fs.mkdirSync('./logs')
}

// OpenTelemetry configuration - send directly to Jaeger
const sdk = new NodeSDK({
  resource: new Resource({
    [SemanticResourceAttributes.SERVICE_NAME]: 'nodejs-app',
    [SemanticResourceAttributes.SERVICE_VERSION]: '1.0.0',
  }),
  traceExporter: new OTLPTraceExporter({
    url: 'http://172.17.0.1:4318/v1/traces',
  }),
  instrumentations: [getNodeAutoInstrumentations({
    '@opentelemetry/instrumentation-fs': {
      enabled: false,
    },
  })],
})

module.exports = { sdk, logger }
