const request = require('supertest')
const UserApp = require('../src/app')

// Mock MongoDB completely
jest.mock('mongodb', () => ({
  MongoClient: jest.fn().mockImplementation(() => ({
    connect: jest.fn().mockResolvedValue(),
    db: jest.fn().mockReturnValue({
      admin: () => ({ ping: jest.fn().mockResolvedValue() }),
      collection: () => ({
        insertOne: jest.fn().mockResolvedValue({ insertedId: 'test-id-123' }),
        find: () => ({
          toArray: jest.fn().mockResolvedValue([
            { _id: 'test-id-123', name: 'Test User', email: 'test@example.com' }
          ])
        }),
        deleteOne: jest.fn().mockResolvedValue({ deletedCount: 1 })
      })
    }),
    close: jest.fn().mockResolvedValue()
  }))
}))

describe('UserApp', () => {
  let app
  let server

  beforeAll(async () => {
    process.env.MONGO_DB = 'userapp_test'
    process.env.MONGO_HOST = 'localhost'
    process.env.NODE_ENV = 'test'
    process.env.PORT = '0' // Use random port for tests
  })

  beforeEach(() => {
    app = new UserApp()
    app.dbConnected = true // Default to connected
    server = app.app
  })

  afterEach(async () => {
    if (app) {
      if (app.healthCheckInterval) {
        clearInterval(app.healthCheckInterval)
        app.healthCheckInterval = null
      }
      if (app.server && app.server.close) {
        await new Promise(resolve => app.server.close(resolve))
      }
      if (app.mongoClient && app.mongoClient.close) {
        await app.mongoClient.close()
      }
    }
  })

  describe('API Endpoints', () => {
    it('should return health status when database connected', async () => {
      app.dbConnected = true
      const response = await request(server).get('/health').expect(200)
      expect(response.body).toHaveProperty('status', 'healthy')
      expect(response.body).toHaveProperty('database', 'connected')
    })

    it('should return metrics', async () => {
      const response = await request(server).get('/metrics').expect(200)
      expect(response.text).toContain('# HELP')
      expect(response.text).toContain('http_requests_total')
    })

    it('should return dashboard info as JSON', async () => {
      const response = await request(server)
        .get('/dashboard')
        .set('Accept', 'application/json')
        .expect(200)
      expect(response.body).toHaveProperty('service', 'Node.js User Service')
      expect(response.body).toHaveProperty('apis')
      expect(response.body.apis).toHaveLength(6)
    })

    it('should render dashboard HTML page', async () => {
      const response = await request(server)
        .get('/dashboard')
        .set('Accept', 'text/html')
        .expect(200)
      expect(response.text).toContain('System Dashboard')
    })

    it('should render registration page', async () => {
      const response = await request(server).get('/').expect(200)
      expect(response.text).toContain('Node.js User Service')
      expect(response.text).toContain('Register User')
    })

    it('should register user when database connected', async () => {
      app.dbConnected = true
      const response = await request(server)
        .post('/register')
        .send({ name: 'John Doe', email: 'john@example.com' })
        .expect(200)
      expect(response.body).toHaveProperty('success', true)
      expect(response.body).toHaveProperty('userId', 'test-id-123')
    })

    it('should validate required fields', async () => {
      const response = await request(server)
        .post('/register')
        .send({ name: 'John Doe' })
        .expect(400)
      expect(response.body).toHaveProperty('error', 'Name and email are required')
    })

    it('should handle database unavailable during registration', async () => {
      app.dbConnected = false
      const response = await request(server)
        .post('/register')
        .send({ name: 'Test', email: 'test@example.com' })
        .expect(503)
      expect(response.body).toHaveProperty('error', 'Database unavailable. Please try again later.')
    })

    it('should return users list when database connected', async () => {
      app.dbConnected = true
      const response = await request(server)
        .get('/users')
        .set('Accept', 'application/json')
        .expect(200)
      expect(Array.isArray(response.body)).toBe(true)
      expect(response.body).toHaveLength(1)
      expect(response.body[0]).toHaveProperty('name', 'Test User')
    })

    it('should handle database unavailable for users list', async () => {
      app.dbConnected = false
      const response = await request(server)
        .get('/users')
        .set('Accept', 'application/json')
        .expect(503)
      expect(response.body).toHaveProperty('error', 'Database unavailable. Please try again later.')
    })

    it('should render users HTML page', async () => {
      app.dbConnected = true
      const response = await request(server)
        .get('/users')
        .set('Accept', 'text/html')
        .expect(200)
      expect(response.text).toContain('Users Management')
    })

    it('should start server on specified port', async () => {
      const testApp = new UserApp()
      const mockServer = { close: jest.fn() }
      testApp.app.listen = jest.fn().mockImplementation((port, callback) => {
        callback()
        return mockServer
      })
      
      const server = await testApp.start()
      expect(server).toBe(mockServer)
      expect(testApp.app.listen).toHaveBeenCalledWith(expect.anything(), expect.any(Function))
    })

    it('should stop server and close connections', async () => {
      const testApp = new UserApp()
      const mockServer = { close: jest.fn() }
      const mockClient = { close: jest.fn() }
      
      testApp.server = mockServer
      testApp.mongoClient = mockClient
      testApp.healthCheckInterval = 123
      
      await testApp.stop()
      
      expect(mockServer.close).toHaveBeenCalled()
      expect(mockClient.close).toHaveBeenCalled()
    })
  })

  describe('Error Handling', () => {
    it('should handle malformed JSON in POST requests', async () => {
      const response = await request(server)
        .post('/register')
        .set('Content-Type', 'application/json')
        .send('invalid json')
        .expect(400)
    })

    it('should handle database errors gracefully', async () => {
      app.dbConnected = true
      app.db = {
        collection: () => ({
          insertOne: jest.fn().mockRejectedValue(new Error('Database error'))
        })
      }
      
      const response = await request(server)
        .post('/register')
        .send({ name: 'Test', email: 'test@example.com' })
        .expect(500)
      expect(response.body).toHaveProperty('error', 'Registration failed')
    })
  })
})
