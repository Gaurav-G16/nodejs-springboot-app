// Initialize OpenTelemetry first
const { sdk, logger } = require('./telemetry')
sdk.start()

const express = require('express')
const { MongoClient } = require('mongodb')
const client = require('prom-client')
const path = require('path')
const os = require('os')

class UserApp {
  constructor () {
    this.app = express()
    this.port = process.env.PORT || 3000
    this.mongoUrl = this.buildMongoUrl()
    this.mongoClient = null
    this.db = null
    this.dbConnected = false

    this.setupLogger()
    this.setupMetrics()
    this.setupMiddleware()
    this.setupRoutes()
    this.connectToDatabase() // Non-blocking connection attempt
  }

  buildMongoUrl () {
    const username = process.env.MONGO_USERNAME || 'admin'
    const password = process.env.MONGO_PASSWORD || 'password123'
    const dbName = process.env.MONGO_DB || 'userapp'
    const host = process.env.MONGO_HOST || 'mongo'
    const port = process.env.MONGO_PORT || '27017'
    return `mongodb://${username}:${password}@${host}:${port}/${dbName}?authSource=admin`
  }

  setupLogger () {
    this.logger = logger
  }

  setupMetrics () {
    this.register = new client.Registry()
    client.collectDefaultMetrics({ register: this.register })

    this.httpRequestsTotal = new client.Counter({
      name: 'http_requests_total',
      help: 'Total HTTP requests',
      labelNames: ['method', 'route', 'status_code'],
      registers: [this.register]
    })

    this.httpRequestDuration = new client.Histogram({
      name: 'http_request_duration_seconds',
      help: 'HTTP request duration in seconds',
      labelNames: ['method', 'route'],
      registers: [this.register]
    })

    this.userRegistrations = new client.Counter({
      name: 'user_registrations_total',
      help: 'Total user registrations',
      registers: [this.register]
    })

    this.dbConnectionStatus = new client.Gauge({
      name: 'database_connection_status',
      help: 'Database connection status (1=connected, 0=disconnected)',
      registers: [this.register]
    })
  }

  setupMiddleware () {
    this.app.use(express.json())
    this.app.use(express.urlencoded({ extended: true }))
    this.app.use(express.static(path.join(__dirname, '../public')))
    this.app.set('view engine', 'ejs')
    this.app.set('views', path.join(__dirname, '../views'))

    // Metrics middleware
    this.app.use((req, res, next) => {
      const start = Date.now()
      res.on('finish', () => {
        const duration = (Date.now() - start) / 1000
        this.httpRequestsTotal.inc({
          method: req.method,
          route: req.route?.path || req.path,
          status_code: res.statusCode
        })
        this.httpRequestDuration.observe({
          method: req.method,
          route: req.route?.path || req.path
        }, duration)
      })
      next()
    })

    // Logging middleware
    this.app.use((req, res, next) => {
      this.logger.info('HTTP Request', {
        method: req.method,
        url: req.url,
        userAgent: req.get('User-Agent'),
        ip: req.ip
      })
      next()
    })
  }

  setupRoutes () {
    this.app.get('/health', async (req, res) => {
      await this.checkDatabaseConnection()
      const healthStatus = this.dbConnected ? 'healthy' : 'degraded'
      res.json({
        status: healthStatus,
        timestamp: new Date().toISOString(),
        database: this.dbConnected ? 'connected' : 'disconnected'
      })
    })

    this.app.get('/metrics', async (req, res) => {
      res.set('Content-Type', this.register.contentType)
      res.end(await this.register.metrics())
    })

    this.app.get('/', async (req, res) => {
      // Use cached status instead of checking every time for root page
      res.render('register', { dbStatus: this.dbConnected })
    })

    // Dashboard route
    this.app.get('/dashboard', async (req, res) => {
      try {
        await this.checkDatabaseConnection()

        const systemInfo = {
          platform: os.platform(),
          arch: os.arch(),
          nodeVersion: process.version,
          uptime: Math.floor(process.uptime()),
          memory: process.memoryUsage(),
          cpus: os.cpus().length
        }

        const dbInfo = {
          type: 'MongoDB',
          status: this.dbConnected ? 'Connected' : 'Disconnected',
          url: this.mongoUrl.replace(/\/\/.*@/, '//***:***@') // Hide credentials
        }

        const apis = [
          { method: 'GET', path: '/', description: 'Registration form' },
          { method: 'POST', path: '/register', description: 'Register new user' },
          { method: 'GET', path: '/users', description: 'List all users' },
          { method: 'GET', path: '/health', description: 'Health check' },
          { method: 'GET', path: '/metrics', description: 'Prometheus metrics' },
          { method: 'GET', path: '/dashboard', description: 'System dashboard' }
        ]

        // Check if request wants HTML or JSON
        if (req.headers.accept && req.headers.accept.includes('text/html')) {
          res.render('dashboard', {
            service: 'Node.js User Service',
            version: '1.0.0',
            system: systemInfo,
            database: dbInfo,
            apis,
            timestamp: new Date().toISOString()
          })
        } else {
          res.json({
            service: 'Node.js User Service',
            version: '1.0.0',
            timestamp: new Date().toISOString(),
            system: systemInfo,
            database: dbInfo,
            apis
          })
        }
      } catch (error) {
        this.logger.error('Dashboard error', { error: error.message })
        res.status(500).json({ error: 'Dashboard temporarily unavailable' })
      }
    })

    this.app.post('/register', async (req, res) => {
      try {
        const { name, email } = req.body
        if (!name || !email) {
          return res.status(400).json({ error: 'Name and email are required' })
        }

        if (!this.dbConnected) {
          return res.status(503).json({ error: 'Database unavailable. Please try again later.' })
        }

        const user = { name, email, createdAt: new Date() }
        const result = await this.db.collection('users').insertOne(user)

        this.userRegistrations.inc()
        this.logger.info('User registered', { userId: result.insertedId, name, email })
        res.json({ success: true, userId: result.insertedId })
      } catch (error) {
        this.logger.error('Registration failed', { error: error.message })
        res.status(500).json({ error: 'Registration failed' })
      }
    })

    this.app.get('/users', async (req, res) => {
      try {
        if (!this.dbConnected) {
          // Check if request wants HTML or JSON
          if (req.headers.accept && req.headers.accept.includes('text/html')) {
            return res.render('users', { users: [], dbConnected: false, error: 'Database unavailable' })
          }
          return res.status(503).json({ error: 'Database unavailable. Please try again later.' })
        }

        const users = await this.db.collection('users').find({}).toArray()

        // Check if request wants HTML or JSON
        if (req.headers.accept && req.headers.accept.includes('text/html')) {
          res.render('users', { users, dbConnected: true, error: null })
        } else {
          res.json(users)
        }
      } catch (error) {
        this.logger.error('Failed to fetch users', { error: error.message })
        if (req.headers.accept && req.headers.accept.includes('text/html')) {
          res.render('users', { users: [], dbConnected: false, error: 'Failed to fetch users' })
        } else {
          res.status(500).json({ error: 'Failed to fetch users' })
        }
      }
    })

    this.app.delete('/users/:id', async (req, res) => {
      try {
        if (!this.dbConnected) {
          return res.status(503).json({ error: 'Database unavailable. Please try again later.' })
        }

        const { id } = req.params
        const { ObjectId } = require('mongodb')

        const result = await this.db.collection('users').deleteOne({ _id: new ObjectId(id) })

        if (result.deletedCount === 1) {
          this.logger.info('User deleted', { userId: id })
          res.json({ success: true, message: 'User deleted successfully' })
        } else {
          res.status(404).json({ error: 'User not found' })
        }
      } catch (error) {
        this.logger.error('Failed to delete user', { error: error.message, userId: req.params.id })
        res.status(500).json({ error: 'Failed to delete user' })
      }
    })
  }

  async connectToDatabase () {
    try {
      this.mongoClient = new MongoClient(this.mongoUrl)
      await this.mongoClient.connect()
      await this.mongoClient.db().admin().ping() // Test connection
      this.db = this.mongoClient.db()
      this.dbConnected = true
      this.dbConnectionStatus.set(1)
      this.logger.info('Connected to MongoDB')
    } catch (error) {
      this.dbConnected = false
      this.dbConnectionStatus.set(0)
      this.logger.warn('MongoDB connection failed - app will continue without database', { error: error.message })
    }
  }

  async checkDatabaseConnection () {
    try {
      if (this.mongoClient) {
        // Set a short timeout for the ping
        const timeoutPromise = new Promise((resolve, reject) =>
          setTimeout(() => reject(new Error('Connection timeout')), 2000)
        )

        await Promise.race([
          this.mongoClient.db().admin().ping(),
          timeoutPromise
        ])

        if (!this.dbConnected) {
          this.dbConnected = true
          this.dbConnectionStatus.set(1)
          this.logger.info('Database connection restored')
        }
      } else {
        throw new Error('No MongoDB client')
      }
    } catch (error) {
      if (this.dbConnected) {
        this.dbConnected = false
        this.dbConnectionStatus.set(0)
        this.logger.warn('Database connection lost', { error: error.message })
      }
    }
    return this.dbConnected
  }

  async start () {
    this.server = this.app.listen(this.port, () => {
      this.logger.info(`Server running on port ${this.port}`)
    })

    // Start periodic database health check (every 10 seconds)
    this.healthCheckInterval = setInterval(() => {
      this.checkDatabaseConnection()
    }, 10000)

    return this.server
  }

  async stop () {
    if (this.healthCheckInterval) {
      clearInterval(this.healthCheckInterval)
    }
    if (this.mongoClient) {
      await this.mongoClient.close()
    }
    if (this.server) {
      this.server.close()
    }
  }
}

if (require.main === module) {
  const app = new UserApp()
  app.start().catch(err => {
    app.logger.error('Failed to start application:', err)
    process.exit(1)
  })
}

module.exports = UserApp
