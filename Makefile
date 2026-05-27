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

dev: env-check ## Start BE (debug + hot reload) + FE (HMR) - full dev stack
	@echo "$(GREEN)Starting full development stack...$(NC)"
	@echo "$(YELLOW)Backend: Spring DevTools hot reload + JDWP debug on :5005$(NC)"
	@echo "$(YELLOW)Frontend: Vite HMR on :5173$(NC)"
	@cd ghost-coach-be/docker && docker compose -f docker-compose.yml -f docker-compose.debug.yml up -d
	@sleep 3
	@cd ghost-coach-web/docker && docker compose -f docker-compose.dev.yml up -d
	@echo ""
	@echo "$(GREEN)✓ Dev stack running:$(NC)"
	@echo "  Backend:  http://localhost:8080  (debug: :5005)"
	@echo "  Frontend: http://localhost:5173"
	@echo "  Postgres: localhost:6432"
	@echo ""
	@echo "$(CYAN)Logs:$(NC) make logs"
	@echo "$(CYAN)Stop:$(NC) make stop"

dev-be: env-check ## Start only backend in debug mode (hot reload + JDWP)
	@echo "$(GREEN)Starting backend (debug + hot reload)...$(NC)"
	@cd ghost-coach-be/docker && docker compose -f docker-compose.yml -f docker-compose.debug.yml up -d
	@echo "$(GREEN)✓ Backend running:$(NC) http://localhost:8080 (debug: :5005)"

dev-fe: ## Start only frontend in dev mode (Vite HMR)
	@echo "$(GREEN)Starting frontend (Vite HMR)...$(NC)"
	@cd ghost-coach-web/docker && docker compose -f docker-compose.dev.yml up -d
	@echo "$(GREEN)✓ Frontend running:$(NC) http://localhost:5173"

##@ Production-like

prod: env-check ## Start BE (prod image) + FE (nginx) - semi-prod stack
	@echo "$(GREEN)Starting production-like stack...$(NC)"
	@cd ghost-coach-be/docker && docker compose -f docker-compose.yml up -d
	@sleep 3
	@cd ghost-coach-web/docker && docker compose -f docker-compose.yml up -d
	@echo ""
	@echo "$(GREEN)✓ Production stack running:$(NC)"
	@echo "  Backend:  http://localhost:8080"
	@echo "  Frontend: http://localhost:5173"
	@echo "  Postgres: localhost:6432"

prod-be: env-check ## Start only backend in production mode
	@echo "$(GREEN)Starting backend (production)...$(NC)"
	@cd ghost-coach-be/docker && docker compose -f docker-compose.yml up -d
	@echo "$(GREEN)✓ Backend running:$(NC) http://localhost:8080"

prod-fe: ## Start only frontend in production mode (nginx)
	@echo "$(GREEN)Starting frontend (nginx)...$(NC)"
	@cd ghost-coach-web/docker && docker compose -f docker-compose.yml up -d
	@echo "$(GREEN)✓ Frontend running:$(NC) http://localhost:5173"

##@ Debugging

debug: dev-be ## Alias for dev-be (backend debug mode)

attach-be: ## Show instructions to attach debugger to backend
	@echo "$(CYAN)Backend JDWP debug port: 5005$(NC)"
	@echo ""
	@echo "$(YELLOW)IntelliJ IDEA:$(NC)"
	@echo "  1. Run → Edit Configurations → + → Remote JVM Debug"
	@echo "  2. Host: localhost, Port: 5005"
	@echo "  3. Click Debug (🐞)"
	@echo ""
	@echo "$(YELLOW)VS Code:$(NC)"
	@echo "  1. Open Run & Debug panel"
	@echo "  2. Select 'Attach to Docker (port 5005)'"
	@echo "  3. Press ▶"

##@ Management

stop: ## Stop all containers (BE + FE)
	@echo "$(YELLOW)Stopping all containers...$(NC)"
	-@cd ghost-coach-be/docker && docker compose -f docker-compose.yml -f docker-compose.debug.yml down 2>/dev/null
	-@cd ghost-coach-web/docker && docker compose -f docker-compose.dev.yml down 2>/dev/null
	-@cd ghost-coach-web/docker && docker compose down 2>/dev/null
	@echo "$(GREEN)✓ All containers stopped$(NC)"

clean: stop ## Stop containers and remove volumes (DB data, uploads, etc)
	@echo "$(RED)Removing all volumes...$(NC)"
	-@cd ghost-coach-be/docker && docker compose -f docker-compose.yml -f docker-compose.debug.yml down -v 2>/dev/null
	-@cd ghost-coach-web/docker && docker compose -f docker-compose.dev.yml down -v 2>/dev/null
	@echo "$(GREEN)✓ Cleanup complete$(NC)"

restart: stop dev ## Restart full dev stack

rebuild: ## Rebuild images and restart dev stack
	@echo "$(YELLOW)Rebuilding images...$(NC)"
	@cd ghost-coach-be/docker && docker compose -f docker-compose.yml -f docker-compose.debug.yml build
	@cd ghost-coach-web/docker && docker compose -f docker-compose.dev.yml build
	@$(MAKE) dev

##@ Monitoring

logs: ## Show logs from all containers
	@echo "$(CYAN)Showing logs (Ctrl+C to exit)...$(NC)"
	@docker compose -f ghost-coach-be/docker/docker-compose.yml logs -f 2>/dev/null || true & \
	docker compose -f ghost-coach-web/docker/docker-compose.dev.yml logs -f 2>/dev/null || true

logs-be: ## Show backend logs only
	@cd ghost-coach-be/docker && docker compose logs -f

logs-fe: ## Show frontend logs only
	@cd ghost-coach-web/docker && docker compose -f docker-compose.dev.yml logs -f

status: ## Show status of all containers
	@echo "$(CYAN)Container Status:$(NC)"
	@docker ps --filter "name=gc-" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

ps: status ## Alias for status

##@ Utilities

env-check: ## Check if .env file exists in backend
	@if [ ! -f ghost-coach-be/.env ]; then \
		echo "$(RED)ERROR: ghost-coach-be/.env not found$(NC)"; \
		echo "$(YELLOW)Run: cp ghost-coach-be/.env.example ghost-coach-be/.env$(NC)"; \
		echo "$(YELLOW)Then edit .env and set JWT_SECRET and GEMINI_API_KEY$(NC)"; \
		exit 1; \
	fi

shell-be: ## Open shell in backend container
	@docker exec -it gc-backend sh 2>/dev/null || docker exec -it gc-backend bash 2>/dev/null || echo "$(RED)Backend container not running$(NC)"

shell-fe: ## Open shell in frontend container
	@docker exec -it gc-web-dev sh 2>/dev/null || docker exec -it gc-web sh 2>/dev/null || echo "$(RED)Frontend container not running$(NC)"

db: ## Connect to PostgreSQL database
	@docker exec -it gc-postgres psql -U postgres -d ghostcoach

prune: ## Remove all unused Docker resources (images, volumes, networks)
	@echo "$(RED)This will remove ALL unused Docker resources. Continue? [y/N]$(NC)" && read ans && [ $${ans:-N} = y ]
	@docker system prune -af --volumes
	@echo "$(GREEN)✓ Docker cleanup complete$(NC)"
