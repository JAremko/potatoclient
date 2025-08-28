# Clj-Stream-Spawner Makefile
# Development and dependency management for the Clojure stream spawner tool

.PHONY: help test run dev deps-outdated deps-upgrade deps-upgrade-all

# Default target
.DEFAULT_GOAL := help

# Help target
help: ## Show available commands
	@echo "Clj-Stream-Spawner Tool"
	@echo "======================="
	@echo ""
	@echo "Available targets:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
	@echo ""
	@echo "For quick testing: make run"
	@echo "For dependency checks: make deps-outdated"

# Run the spawner
run: ## Run the stream spawner (launches video streams)
	@echo "Starting stream spawner..."
	clojure -M:run

# Development REPL
dev: ## Start development REPL with CIDER support
	@echo "Starting development REPL..."
	clojure -M:dev

# Run tests
test: ## Run all tests
	@echo "Running tests..."
	clojure -M:test

# Check for outdated dependencies
deps-outdated: ## Check for outdated dependencies using antq
	@echo "Checking for outdated dependencies..."
	@echo "  • Scans deps.edn for newer versions"
	@echo "  • Shows available updates in a table"
	@echo "  • Includes both direct and transitive dependencies"
	@echo ""
	@cd ../.. && clojure -M:outdated --directory=tools/clj-stream-spawner

# Interactively upgrade dependencies
deps-upgrade: ## Interactively upgrade outdated dependencies
	@echo "Interactive dependency upgrade..."
	@echo "  • Shows each outdated dependency"
	@echo "  • Lets you choose which to upgrade"
	@echo "  • Automatically updates deps.edn"
	@echo "  • Downloads new dependencies"
	@echo ""
	@cd ../.. && clojure -M:outdated-upgrade --directory=tools/clj-stream-spawner

# Force upgrade all dependencies
deps-upgrade-all: ## Upgrade all outdated dependencies (non-interactive)
	@echo "Upgrading all outdated dependencies..."
	@echo "  ⚠️  This will automatically update ALL outdated dependencies"
	@echo "  • Updates deps.edn without confirmation"
	@echo "  • Downloads all new versions"
	@echo "  • Remember to test after upgrading!"
	@echo ""
	@read -p "Are you sure? (y/N) " -n 1 -r; \
	echo ""; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		cd ../.. && clojure -M:outdated-upgrade --force --directory=tools/clj-stream-spawner; \
		echo "✓ Dependencies upgraded. Remember to run 'make test' to verify!"; \
	else \
		echo "Upgrade cancelled."; \
	fi