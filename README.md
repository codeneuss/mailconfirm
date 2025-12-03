# MailConfirm

A professional email verification service built with Quarkus that provides opt-in email confirmation functionality.

[![GitHub](https://img.shields.io/badge/GitHub-codeneuss%2Fmailconfirm-blue?logo=github)](https://github.com/codeneuss/mailconfirm)
[![Quarkus](https://img.shields.io/badge/Quarkus-3.16.3-blue?logo=quarkus)](https://quarkus.io)
[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

## ⚠️ Important Security Notice

**THIS IS 100% DEMO/VIBE CODE - NOT FOR PRODUCTION USE!**

This project is created for educational and demonstration purposes only. It is **NOT production-ready** and should **NEVER** be used in a production environment without significant security hardening and proper implementation of:

- Database persistence (currently uses in-memory storage)
- Proper authentication and authorization
- Rate limiting and anti-spam measures
- Security headers and CORS configuration
- Input validation and sanitization
- Proper secret management
- Monitoring and logging
- GDPR compliance and data protection
- Production-grade email service integration
- Comprehensive error handling and recovery
- Security audits and penetration testing

**Use at your own risk. This code is provided as-is for learning purposes only.**

## Features

- RESTful API for sending verification emails
- Beautiful HTML email templates
- Token-based email verification
- Swagger UI API documentation
- Docker Compose setup with fake SMTP server (MailHog)
- Health check endpoints
- Clean Code architecture with separation of concerns

## Technology Stack

- **Quarkus 3.16.3** - Supersonic Subatomic Java Framework
- **Java 21** - Latest LTS version
- **Qute** - Template engine for email rendering
- **Quarkus Mailer** - Email sending functionality
- **SmallRye OpenAPI** - API documentation
- **MailHog** - Fake SMTP server for testing
- **Docker & Docker Compose** - Containerization

## Project Structure

```
src/main/java/com/emailverification/
├── api/                          # REST API endpoints
│   ├── EmailVerificationResource.java
│   └── HealthResource.java
├── domain/                       # Domain models
│   ├── EmailVerificationRequest.java
│   ├── EmailVerificationResponse.java
│   └── VerificationToken.java
├── repository/                   # Data access layer
│   └── VerificationTokenRepository.java
└── service/                      # Business logic
    ├── EmailService.java
    └── VerificationService.java

src/main/resources/
├── application.properties        # Application configuration
└── templates/
    └── verification-email.html   # Email template
```

## Prerequisites

- Docker and Docker Compose
- Java 21 (for local development)
- Maven 3.9+ (for local development)

## Getting Started

### Quick Start with Make Commands

This project includes a Makefile with convenient commands for common tasks. To see all available commands:

```bash
make help
```

#### Essential Commands

**Start the Application:**
```bash
make up          # Build and start all services in background
make quick-start # Alias for 'make up'
```

**Stop the Application:**
```bash
make down        # Stop and remove all containers
make stop        # Stop containers without removing them
```

**Development:**
```bash
make dev         # Run application locally in development mode
make test        # Run tests
make build       # Build the application with Maven
```

**Monitoring:**
```bash
make logs        # Show logs from all containers
make logs-app    # Show application logs only
make logs-mailhog # Show MailHog logs only
make status      # Show status of all containers
make health      # Check application health
```

**Quick Access:**
```bash
make swagger     # Open Swagger UI in browser
make mailhog     # Open MailHog UI in browser
```

**Maintenance:**
```bash
make clean       # Clean build artifacts and remove containers
make restart     # Restart all services
make rebuild     # Clean, build, and start fresh
```

#### All Available Make Commands

| Command | Description |
|---------|-------------|
| `make help` | Show all available commands with descriptions |
| `make build` | Build the application with Maven |
| `make up` | Start all services with Docker Compose |
| `make down` | Stop and remove all containers |
| `make start` | Start existing containers |
| `make stop` | Stop running containers |
| `make restart` | Restart all services |
| `make logs` | Show logs from all containers (follow mode) |
| `make logs-app` | Show logs from application container only |
| `make logs-mailhog` | Show logs from MailHog container only |
| `make clean` | Clean build artifacts and stop containers |
| `make test` | Run tests |
| `make dev` | Run application in development mode (without Docker) |
| `make docker-build` | Build Docker image |
| `make health` | Check application health endpoints |
| `make swagger` | Open Swagger UI in browser |
| `make mailhog` | Open MailHog UI in browser |
| `make status` | Show status of all containers |
| `make test-flow` | Run the test flow script |
| `make install` | Install Maven dependencies |
| `make format` | Format code |
| `make verify` | Verify the build |
| `make package` | Package the application |
| `make rebuild` | Clean, build, and start fresh |
| `make quick-start` | Quick start (alias for up) |
| `make shell-app` | Open shell in application container |
| `make shell-mailhog` | Open shell in MailHog container |

### Running with Docker Compose

1. Build and start the services:
```bash
make up
# or
docker-compose up --build
```

2. The following services will be available:
   - **Application API**: http://localhost:8080
   - **Swagger UI**: http://localhost:8080/swagger-ui
   - **MailHog Web UI**: http://localhost:8025
   - **OpenAPI Spec**: http://localhost:8080/openapi

### Running Locally

1. Start MailHog:
```bash
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

2. Run the application:
```bash
make dev
# or
./mvnw quarkus:dev
```

## API Endpoints

### 1. Send Verification Email

**Endpoint:** `POST /api/send-verification`

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response (202 Accepted):**
```json
{
  "message": "Verification email sent successfully. Please check your inbox.",
  "email": "user@example.com"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/send-verification \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com"}'
```

### 2. Verify Email Address

**Endpoint:** `GET /api/verify?token={token}`

**Parameters:**
- `token` (query parameter) - Verification token from email

**Response:** HTML page showing verification success or error

**Example:**
```
http://localhost:8080/api/verify?token=123e4567-e89b-12d3-a456-426614174000
```

### 3. Health Checks

- **Readiness:** `GET /q/health/ready`
- **Liveness:** `GET /q/health/live`

## Testing the Flow

1. Start the application using Docker Compose:
```bash
make up
```

2. Send a verification email:
```bash
curl -X POST http://localhost:8080/api/send-verification \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com"}'
```

3. Open MailHog UI to view the received email:
```bash
make mailhog
# or manually open http://localhost:8025
```

4. Click the verification button in the email or copy the verification link

5. The verification link will redirect you to a success page confirming the email verification

**Run automated test flow:**
```bash
make test-flow
```

## API Documentation

Access the interactive API documentation at:
- **Swagger UI**: http://localhost:8080/swagger-ui

The Swagger UI provides:
- Complete API documentation
- Interactive testing capabilities
- Request/response schemas
- Example payloads

## Configuration

Key configuration properties in `application.properties`:

```properties
# Application
quarkus.application.name=Email Verification Service
quarkus.http.port=8080

# Mailer
quarkus.mailer.from=noreply@emailverification.com
quarkus.mailer.host=localhost
quarkus.mailer.port=1025

# Application Base URL
app.base-url=http://localhost:8080

# Token Expiry
verification.token.expiry-hours=24
```

## Architecture & Design Principles

This application follows Clean Code and SOLID principles:

- **Separation of Concerns**: Clear separation between API, service, and repository layers
- **Single Responsibility**: Each class has one well-defined purpose
- **Dependency Injection**: Using CDI for loose coupling
- **Validation**: Bean Validation for input validation
- **Error Handling**: Proper exception handling with meaningful messages
- **Immutability**: Using Java records for DTOs
- **Async Processing**: Non-blocking email sending with CompletionStage

## Security Considerations

- Email validation using Jakarta Bean Validation
- Token-based verification with expiry
- No sensitive information in logs
- Non-root user in Docker container

## Troubleshooting

### Application won't start
- Ensure ports 8080, 1025, and 8025 are not in use
- Check Docker and Docker Compose are installed and running

### Emails not being received
- Verify MailHog is running: http://localhost:8025
- Check application logs for email sending errors
- Ensure mailer configuration is correct

### Verification link doesn't work
- Verify the token hasn't expired (24 hours by default)
- Check that app.base-url is set correctly
- Ensure the token is valid and hasn't been used already

## Development

### Building the project
```bash
make build
# or
./mvnw clean package
```

### Running tests
```bash
make test
# or
./mvnw test
```

### Building Docker image
```bash
make docker-build
# or
docker build -t mailconfirm:latest .
```

### Development workflow
```bash
# Format code
make format

# Verify build
make verify

# Package application
make package

# Install dependencies
make install

# Run in dev mode with hot reload
make dev
```

### Debugging with container shell access
```bash
# Access application container
make shell-app

# Access MailHog container
make shell-mailhog
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

**Disclaimer:** This is 100% demonstration/vibe code created for educational purposes. It is NOT suitable for production use and should only be used for learning, testing, and demonstration purposes. See the security notice at the top of this README for more information.
