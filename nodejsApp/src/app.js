require('./telemetry')

const express = require('express')
const { MongoClient } = require('mongodb')
const client = require('prom-client')
const winston = require('winston')
const path = require('path')

class UserApp {
  constructor () {
    this.app = express()
    this.port = process.env.PORT || 3000
    this.mongoUrl = this.buildMongoUrl()
    this.mongoClient = null
    this.db = null

    this.setupLogger()
    this.setupMetrics()
    this.setupMiddleware()
    this.setupRoutes()
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
    this.logger = winston.createLogger({
      level: 'info',
      format: winston.format.combine(
        winston.format.timestamp(),
        winston.format.json()
      ),
      transports: [
        new winston.transports.Console()
      ]
    })
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
    this.app.get('/health', (req, res) => {
      res.json({ status: 'healthy', timestamp: new Date().toISOString() })
    })

    this.app.get('/metrics', async (req, res) => {
      res.set('Content-Type', this.register.contentType)
      res.end(await this.register.metrics())
    })

    this.app.get('/', (req, res) => {
      res.render('register')
    })

    this.app.post('/register', async (req, res) => {
      try {
        const { name, email } = req.body
        if (!name || !email) {
          return res.status(400).json({ error: 'Name and email are required' })
        }

        const user = { name, email, createdAt: new Date() }
        const result = await this.db.collection('users').insertOne(user)

        this.logger.info('User registered', { userId: result.insertedId, name, email })
        res.json({ success: true, userId: result.insertedId })
      } catch (error) {
        this.logger.error('Registration failed', { error: error.message })
        res.status(500).json({ error: 'Registration failed' })
      }
    })

    this.app.get('/users', async (req, res) => {
      try {
        const users = await this.db.collection('users').find({}).toArray()
        res.json(users)
      } catch (error) {
        this.logger.error('Failed to fetch users', { error: error.message })
        res.status(500).json({ error: 'Failed to fetch users' })
      }
    })
  }

  async connectToDatabase () {
    try {
      this.mongoClient = new MongoClient(this.mongoUrl)
      await this.mongoClient.connect()
      this.db = this.mongoClient.db()
      this.logger.info('Connected to MongoDB')
    } catch (error) {
      this.logger.error('MongoDB connection failed', { error: error.message })
      throw error
    }
  }

  async start () {
    try {
      await this.connectToDatabase()
      this.server = this.app.listen(this.port, () => {
        this.logger.info(`Server running on port ${this.port}`)
      })
      return this.server
    } catch (error) {
      this.logger.error('Failed to start server', { error: error.message })
      throw error
    }
  }

  async stop () {
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
    console.error('Failed to start application:', err)
    process.exit(1)
  })
}

module.exports = UserApp
