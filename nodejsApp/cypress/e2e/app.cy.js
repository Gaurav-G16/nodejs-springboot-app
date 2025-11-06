describe('User App E2E Tests', () => {
  it('should load the homepage', () => {
    cy.visit('/')
    cy.contains('User Registration')
  })

  it('should register a new user', () => {
    cy.visit('/')
    cy.get('input[name="name"]').type('Test User')
    cy.get('input[name="email"]').type('test@example.com')
    cy.get('form').submit()
    cy.contains('success')
  })

  it('should access health endpoint', () => {
    cy.request('/health').then((response) => {
      expect(response.status).to.eq(200)
      expect(response.body).to.have.property('status', 'healthy')
    })
  })

  it('should access metrics endpoint', () => {
    cy.request('/metrics').then((response) => {
      expect(response.status).to.eq(200)
      expect(response.body).to.contain('# HELP')
    })
  })

  it('should fetch users', () => {
    cy.request('/users').then((response) => {
      expect(response.status).to.eq(200)
      expect(response.body).to.be.an('array')
    })
  })
})
