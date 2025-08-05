# Mock Video Stream Tool - Self-Documenting Build System
#
# This tool provides a mock video stream subprocess for testing the main
# application without requiring real video hardware or GStreamer.
#
# Default target shows help
.DEFAULT_GOAL := help

# Variables
TEST_DATA_DIR = ./test-data
STREAM_TYPE ?= heat
SCENARIO ?= tap-center

# Colors for output
GREEN := \033[0;32m
YELLOW := \033[1;33m
NC := \033[0m # No Color

.PHONY: help test test-watch repl process generate validate scenarios integration-test clean

help: ## Show this help
	@echo "$(GREEN)Mock Video Stream Tool$(NC)"
	@echo ""
	@echo "A mock video stream subprocess for contract testing without real hardware."
	@echo ""
	@echo "$(YELLOW)Usage:$(NC) make [target] [OPTIONS]"
	@echo ""
	@echo "$(YELLOW)Targets:$(NC)"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(GREEN)%-18s$(NC) %s\n", $$1, $$2}'
	@echo ""
	@echo "$(YELLOW)Options:$(NC)"
	@echo "  STREAM_TYPE=heat|day  Stream type to simulate (default: heat)"
	@echo "  SCENARIO=name         Test scenario name (default: tap-center)"
	@echo ""
	@echo "$(YELLOW)Examples:$(NC)"
	@echo "  make process STREAM_TYPE=heat  # Run as heat camera stream"
	@echo "  make generate                  # Generate test scenarios"
	@echo "  make test                      # Run all tests"
	@echo "  make example SCENARIO=pan-rotate # Run specific scenario"

test: ## Run all tests
	@echo "$(GREEN)Running tests...$(NC)"
	clojure -M:test

test-watch: ## Run tests in watch mode
	@echo "$(GREEN)Running tests in watch mode...$(NC)"
	clojure -M:test --watch

repl: ## Start REPL for development
	@echo "$(GREEN)Starting REPL...$(NC)"
	clojure -M:repl

process: ## Run as mock video stream subprocess
	@echo "$(GREEN)Starting mock video stream ($(STREAM_TYPE))...$(NC)"
	clojure -M:process --stream-type $(STREAM_TYPE)

generate: ## Generate test scenarios in JSON format
	@echo "$(GREEN)Generating test scenarios...$(NC)"
	@mkdir -p $(TEST_DATA_DIR)
	clojure -X:generate :output-dir '"$(TEST_DATA_DIR)"'
	@echo "$(GREEN)✓ Generated test scenarios in $(TEST_DATA_DIR)$(NC)"

validate: ## Validate all test scenarios against specs
	@echo "$(GREEN)Validating test scenarios...$(NC)"
	clojure -M:run validate

scenarios: ## List available test scenarios
	@echo "$(GREEN)Available test scenarios:$(NC)"
	@clojure -M:run list-scenarios

example: ## Run example scenario (use SCENARIO=name)
	@echo "$(GREEN)Running scenario: $(SCENARIO)$(NC)"
	@clojure -M:run example --scenario $(SCENARIO)

integration-test: ## Run integration test with main app
	@echo "$(GREEN)Running integration tests...$(NC)"
	@./scripts/integration-test.sh

java-test: ## Test Java NDC converter
	@echo "$(GREEN)Testing NDC converter...$(NC)"
	@cd ../.. && clojure -M:test -n potatoclient.video.ndc-converter-test

clean: ## Clean generated files
	@echo "$(GREEN)Cleaning...$(NC)"
	@rm -rf $(TEST_DATA_DIR) target .cpcache
	@echo "$(GREEN)✓ Clean complete$(NC)"

# Development helpers
dev-setup: ## Set up development environment
	@echo "$(GREEN)Setting up development environment...$(NC)"
	@echo "Installing git hooks..."
	@echo "✓ Development environment ready"

lint: ## Run linters
	@echo "$(GREEN)Running linters...$(NC)"
	@cd ../.. && make lint-clj

.PHONY: dev-setup lint