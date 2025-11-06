// Global test setup
process.env.NODE_ENV = 'test'
process.env.MONGO_HOST = 'localhost'
process.env.MONGO_DB = 'userapp_test'

// Increase timeout for async operations
jest.setTimeout(10000)

// Clean up after each test
afterEach(() => {
  // Clear all timers
  jest.clearAllTimers()
})

// Global cleanup
afterAll(async () => {
  // Force close any remaining handles
  await new Promise(resolve => setTimeout(resolve, 100))
})
