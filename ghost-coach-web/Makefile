.PHONY: help dev prod debug stop clean logs status env-check

# Default target
.DEFAULT_GOAL := help

# Colors for output
CYAN := \033[0;36m
GREEN := \033[0;32m
YELLOW := \033[0;33m
RED := \033[0;31m
NC := \033[0m # No Color

##@ General

help: ## Show this help message
	@echo "$(CYAN)Ghost Coach - Docker Management$(NC)"
	@echo ""
	@awk 'BEGIN {FS = ":.*##"; printf "Usage:\n  make $(CYAN)<target>$(NC)\n"} /^[a-zA-Z_-]+:.*?##/ { printf "  $(CYAN)%-20s$(NC) %s\n", $$1, $$2 } /^##@/ { printf "\n$(YELLOW)%s$(NC)\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

##@ Development (Hot Reload)

dev: env-check ## Start FE (HMR) - full dev stack
	@echo "$(GREEN)Starting full development stack...$(NC)"
	@echo "$(YELLOW)Frontend: Vite HMR on :5173$(NC)"
	@cd ghost-coach-web/docker && docker compose -f docker-compose.dev.yml up -d
	@sleep 3
	@echo ""
	@echo "$(GREEN)✓ Dev stack running:$(NC)"
	@echo "  Backend:  http://localhost:8080  (debug: :5005)"
	@echo "  Frontend: http://localhost:5173"
	@echo "  Postgres: localhost:6432"
	@echo ""
	@echo "$(CYAN)Logs:$(NC) make logs"
	@echo "$(CYAN)Stop:$(NC) make stop"

prod: env-check ## Start FE (nginx) - semi-prod stack
	@echo "$(GREEN)Starting production-like stack...$(NC)"
	@cd ghost-coach-web/docker && docker compose -f docker-compose.yml up -d
	@sleep 3
	@echo ""
	@echo "$(GREEN)✓ Production stack running:$(NC)"
	@echo "  Backend:  http://localhost:8080"
	@echo "  Frontend: http://localhost:5173"
	@echo "  Postgres: localhost:6432"

##@ Management

stop: ## Stop all containers (FE)
	@echo "$(YELLOW)Stopping all containers...$(NC)"
	-@cd ghost-coach-web/docker && docker compose -f docker-compose.dev.yml down 2>/dev/null
	-@cd ghost-coach-web/docker && docker compose down 2>/dev/null
	@echo "$(GREEN)✓ All containers stopped$(NC)"

clean: stop ## Stop containers and remove volumes
	@echo "$(RED)Removing all volumes...$(NC)"
	-@cd ghost-coach-web/docker && docker compose -f docker-compose.dev.yml down -v 2>/dev/null
	@echo "$(GREEN)✓ Cleanup complete$(NC)"

restart: stop dev ## Restart full dev stack

rebuild: ## Rebuild images and restart dev stack
	@echo "$(YELLOW)Rebuilding images...$(NC)"
	@cd ghost-coach-web/docker && docker compose -f docker-compose.dev.yml build
	@$(MAKE) dev

##@ Monitoring

logs: ## Show logs from all containers
	@echo "$(CYAN)Showing logs (Ctrl+C to exit)...$(NC)"
	docker compose -f ghost-coach-web/docker/docker-compose.dev.yml logs -f 2>/dev/null || true

status: ## Show status of all containers
	@echo "$(CYAN)Container Status:$(NC)"
	@docker ps --filter "name=gc-" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

ps: status ## Alias for status

##@ Utilities

env-check: ## Check if .env file exists in backend
	@if [ ! -f ghost-coach-web/.env ]; then \
		echo "$(RED)ERROR: ghost-coach-web/.env not found$(NC)"; \
		echo "$(YELLOW)Run: cp ghost-coach-web/.env.example ghost-coach-web/.env$(NC)"; \
		echo "$(YELLOW)Then edit .env and set JWT_SECRET and GEMINI_API_KEY$(NC)"; \
		exit 1; \
	fi

shell-fe: ## Open shell in frontend container
	@docker exec -it gc-web-dev sh 2>/dev/null || docker exec -it gc-web sh 2>/dev/null || echo "$(RED)Frontend container not running$(NC)"

prune: ## Remove all unused Docker resources (images, volumes, networks)
	@echo "$(RED)This will remove ALL unused Docker resources. Continue? [y/N]$(NC)" && read ans && [ $${ans:-N} = y ]
	@docker system prune -af --volumes
	@echo "$(GREEN)✓ Docker cleanup complete$(NC)"
