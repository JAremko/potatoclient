# Shared Module Makefile
# Manages proto compilation and shared library builds

.PHONY: help clean test compile proto-update deps-outdated deps-upgrade kondo-configs fmt lint

# Default target
.DEFAULT_GOAL := help

# Help target
help: ## Show available commands
	@echo "Shared Module Build System"
	@echo "=========================="
	@echo ""
	@echo "Available targets:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
	@echo ""
	@echo "Note: Proto Java sources are in src/java/ and should be checked into git"
	@echo "Use 'make proto-update' only when .proto files change upstream"

# Clean build artifacts only (never touches sources)
clean: ## Clean build artifacts (target directory only)
	@echo "Cleaning build artifacts..."
	@clojure -T:build clean
	@echo "Build artifacts cleaned"

# Compile all sources
compile: ## Compile all Java and Clojure sources
	@if [ ! -d "src/java" ]; then \
		echo "ERROR: Java sources not found in src/java/"; \
		echo "Proto sources should be checked into git."; \
		echo "If you need to regenerate them, run: make proto-update"; \
		exit 1; \
	fi
	@echo "Compiling all sources..."
	@clojure -T:build compile-all
	@echo "Compilation complete"

# Run tests
test: compile ## Run all shared module tests
	@echo "Running shared module tests..."
	@clojure -M:test

# Update proto sources when .proto files change
proto-update: ## Regenerate proto Java sources from .proto files
	@echo "================================================"
	@echo "Updating Proto Java Sources"
	@echo "================================================"
	@echo "This will regenerate Java sources from .proto files."
	@echo "Only run this when the upstream .proto files have changed."
	@echo ""
	@read -p "Continue? (y/N) " -n 1 -r; \
	echo ""; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		echo "Generating proto Java sources..."; \
		./scripts/generate-protos.sh || (echo "Proto generation failed, trying fallback..." && \
			mkdir -p src/java && \
			cp -r ../examples/protogen/output/java/* src/java/ 2>/dev/null && \
			echo "Used fallback proto sources from examples/protogen/output/java/"); \
		echo "Proto sources updated. Remember to commit the changes."; \
	else \
		echo "Cancelled."; \
	fi

# Dependency management
deps-outdated: ## Check for outdated dependencies
	@echo "Checking for outdated dependencies..."
	@cd .. && clojure -M:outdated --directory=shared

deps-upgrade: ## Interactively upgrade outdated dependencies
	@echo "Interactive dependency upgrade..."
	@cd .. && clojure -M:outdated-upgrade --directory=shared

# Generate clj-kondo configs
kondo-configs: compile ## Generate clj-kondo type configs from Malli specs
	@echo "Generating clj-kondo type configs..."
	@cd .. && clojure -M:dev scripts/generate-kondo-configs.clj shared
	@echo "Type configs generated"

# Format code
fmt: ## Format Clojure code
	@echo "Formatting Clojure code..."
	@cd .. && clojure -M:format fix shared/src shared/test
	@echo "Formatting complete"

# Lint code
lint: ## Lint Clojure code with clj-kondo
	@echo "Linting Clojure code..."
	@cd .. && clojure -M:lint --lint shared/src shared/test
	@echo "Linting complete"

# Test specific categories (kept for convenience)
test-oneof: compile ## Run only oneof custom spec tests
	@echo "Running oneof tests..."
	@clojure -M:test-oneof

test-malli: compile ## Run only malli spec tests
	@echo "Running malli spec tests..."
	@clojure -M:test-malli

test-serialization: compile ## Run only serialization tests
	@echo "Running serialization tests..."
	@clojure -M:test-serialization

test-cmd: compile ## Run only command tests
	@echo "Running command tests..."
	@clojure -M:test-cmd