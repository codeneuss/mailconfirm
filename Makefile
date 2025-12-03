.PHONY: help build up down start stop restart logs clean test dev docker-build docker-push health swagger mailhog

# Variables
DOCKER_COMPOSE = docker-compose
MVN = ./mvnw
APP_URL = http://localhost:8080
MAILHOG_URL = http://localhost:8025
SWAGGER_URL = http://localhost:8080/swagger-ui

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

build: ## Build the application with Maven
	$(MVN) clean package

up: ## Start all services with Docker Compose
	$(DOCKER_COMPOSE) up --build -d
	@echo "Services started!"
	@echo "Application: $(APP_URL)"
	@echo "Swagger UI: $(SWAGGER_URL)"
	@echo "MailHog UI: $(MAILHOG_URL)"

down: ## Stop and remove all containers
	$(DOCKER_COMPOSE) down

start: ## Start existing containers
	$(DOCKER_COMPOSE) start

stop: ## Stop running containers
	$(DOCKER_COMPOSE) stop

restart: ## Restart all services
	$(DOCKER_COMPOSE) restart

logs: ## Show logs from all containers
	$(DOCKER_COMPOSE) logs -f

logs-app: ## Show logs from application container only
	$(DOCKER_COMPOSE) logs -f app

logs-mailhog: ## Show logs from MailHog container only
	$(DOCKER_COMPOSE) logs -f mailhog

clean: ## Clean build artifacts and stop containers
	$(DOCKER_COMPOSE) down -v
	$(MVN) clean
	@echo "Cleanup complete!"

test: ## Run tests
	$(MVN) test

dev: ## Run application in development mode (without Docker)
	@echo "Make sure MailHog is running: docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog"
	$(MVN) quarkus:dev

docker-build: ## Build Docker image
	docker build -t mailconfirm:latest .

health: ## Check application health
	@echo "Checking application health..."
	@curl -f $(APP_URL)/q/health/ready && echo "\nApplication is ready!" || echo "\nApplication is not ready!"

swagger: ## Open Swagger UI in browser
	@echo "Opening Swagger UI..."
	@open $(SWAGGER_URL) || xdg-open $(SWAGGER_URL) || echo "Please open $(SWAGGER_URL) in your browser"

mailhog: ## Open MailHog UI in browser
	@echo "Opening MailHog UI..."
	@open $(MAILHOG_URL) || xdg-open $(MAILHOG_URL) || echo "Please open $(MAILHOG_URL) in your browser"

status: ## Show status of all containers
	$(DOCKER_COMPOSE) ps

test-flow: ## Run the test flow script
	@bash test-flow.sh

install: ## Install Maven dependencies
	$(MVN) install -DskipTests

format: ## Format code
	$(MVN) formatter:format

verify: ## Verify the build
	$(MVN) verify

package: ## Package the application
	$(MVN) package

rebuild: clean build up ## Clean, build, and start fresh

quick-start: up ## Quick start (alias for up)

shell-app: ## Open shell in application container
	docker exec -it mailconfirm-app /bin/sh

shell-mailhog: ## Open shell in MailHog container
	docker exec -it mailconfirm-mailhog /bin/sh
