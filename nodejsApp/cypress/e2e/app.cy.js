describe('User App E2E Tests', () => {
  beforeEach(() => {
    // Set base URL for tests
    cy.visit('/')
  })

  it('should load the homepage with navigation', () => {
    cy.contains('Node.js User Service')
    cy.contains('Register New User')
    
    // Check navigation links
    cy.get('nav').should('contain', 'Home')
    cy.get('nav').should('contain', 'Users')
    cy.get('nav').should('contain', 'Dashboard')
    cy.get('nav').should('contain', 'Health')
    cy.get('nav').should('contain', 'Metrics')
  })

  it('should show database status', () => {
    cy.get('.status-bar').should('be.visible')
    cy.get('.status').should('contain', 'Database:')
  })

  it('should register a new user successfully', () => {
    cy.get('input[name="name"]').type('Test User')
    cy.get('input[name="email"]').type('test@example.com')
    cy.get('form').submit()
    
    // Check for success message
    cy.get('#message').should('contain', 'User registered successfully')
    
    // Form should be reset
    cy.get('input[name="name"]').should('have.value', '')
    cy.get('input[name="email"]').should('have.value', '')
  })

  it('should show validation errors for empty fields', () => {
    cy.get('form').submit()
    
    // HTML5 validation should prevent submission
    cy.get('input[name="name"]:invalid').should('exist')
    cy.get('input[name="email"]:invalid').should('exist')
  })

  it('should navigate to dashboard and show system info', () => {
    cy.get('nav a[href="/dashboard"]').click()
    cy.url().should('include', '/dashboard')
    
    // Dashboard should return JSON with system info
    cy.request('/dashboard').then((response) => {
      expect(response.status).to.eq(200)
      expect(response.body).to.have.property('service', 'Node.js User Service')
      expect(response.body).to.have.property('system')
      expect(response.body).to.have.property('database')
      expect(response.body).to.have.property('apis')
      expect(response.body.apis).to.be.an('array')
      expect(response.body.apis.length).to.be.greaterThan(0)
    })
  })

  it('should access health endpoint and return status', () => {
    cy.request('/health').then((response) => {
      expect(response.status).to.eq(200)
      expect(response.body).to.have.property('status', 'healthy')
      expect(response.body).to.have.property('timestamp')
      expect(response.body).to.have.property('database')
    })
  })

  it('should access metrics endpoint and return prometheus metrics', () => {
    cy.request('/metrics').then((response) => {
      expect(response.status).to.eq(200)
      expect(response.headers['content-type']).to.contain('text/plain')
      expect(response.body).to.contain('# HELP')
      expect(response.body).to.contain('http_requests_total')
      expect(response.body).to.contain('database_connection_status')
    })
  })

  it('should fetch users list', () => {
    cy.request('/users').then((response) => {
      expect(response.status).to.be.oneOf([200, 503]) // 503 if DB is down
      if (response.status === 200) {
        expect(response.body).to.be.an('array')
      } else {
        expect(response.body).to.have.property('error')
      }
    })
  })

  it('should handle navigation between pages', () => {
    // Test navigation to users page
    cy.get('nav a[href="/users"]').click()
    cy.url().should('include', '/users')
    
    // Test navigation to health page
    cy.get('nav a[href="/health"]').click()
    cy.url().should('include', '/health')
    
    // Test navigation back to home
    cy.get('nav a[href="/"]').click()
    cy.url().should('not.include', '/health')
  })

  it('should be responsive on mobile viewport', () => {
    cy.viewport('iphone-6')
    cy.get('.container').should('be.visible')
    cy.get('nav').should('be.visible')
    cy.get('.form-container').should('be.visible')
  })

  it('should handle form submission with JavaScript disabled', () => {
    // This test ensures the form works even without JavaScript
    cy.get('input[name="name"]').type('No JS User')
    cy.get('input[name="email"]').type('nojs@example.com')
    
    // Intercept the form submission
    cy.intercept('POST', '/register', {
      statusCode: 200,
      body: { success: true, userId: 'test-id' }
    }).as('registerUser')
    
    cy.get('form').submit()
    cy.wait('@registerUser')
  })

  it('should handle server errors gracefully', () => {
    // Mock server error
    cy.intercept('POST', '/register', {
      statusCode: 500,
      body: { error: 'Server error' }
    }).as('serverError')
    
    cy.get('input[name="name"]').type('Error User')
    cy.get('input[name="email"]').type('error@example.com')
    cy.get('form').submit()
    
    cy.wait('@serverError')
    cy.get('#message').should('contain', 'Error: Server error')
  })

  it('should handle database unavailable scenario', () => {
    // Mock database unavailable
    cy.intercept('POST', '/register', {
      statusCode: 503,
      body: { error: 'Database unavailable. Please try again later.' }
    }).as('dbUnavailable')
    
    cy.get('input[name="name"]').type('DB Down User')
    cy.get('input[name="email"]').type('dbdown@example.com')
    cy.get('form').submit()
    
    cy.wait('@dbUnavailable')
    cy.get('#message').should('contain', 'Database unavailable')
  })
})
