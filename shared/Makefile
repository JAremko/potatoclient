# Shared Project Makefile
# Central proto generation and compilation for the entire PotatoClient project

.PHONY: proto proto-clean test compile help

# Default target
.DEFAULT_GOAL := help

# Help target
help: ## Show available commands
	@echo "Shared Module - Proto Management"
	@echo "================================"
	@echo ""
	@echo "Available targets:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
	@echo ""
	@echo "IMPORTANT: Run 'make proto' when proto files change"
	@echo "Proto classes are preserved between builds unless explicitly regenerated"

# Generate and compile proto classes (only run when protos change)
proto: proto-clean ## Regenerate proto Java sources and compile them
	@echo "================================================"
	@echo "Regenerating Proto Classes"
	@echo "================================================"
	@echo "Cleaning old proto artifacts..."
	@rm -rf src/java target/classes/cmd target/classes/ser
	@echo "Generating proto Java sources..."
	./scripts/generate-protos.sh
	@echo "Compiling proto Java sources to bytecode..."
	clojure -T:build compile-proto-sources
	@echo "================================================"
	@echo "✓ Proto generation and compilation complete!"
	@echo "  Java sources: src/java/"
	@echo "  Compiled classes: target/classes/"
	@echo "================================================"

# Clean only proto artifacts (use before regenerating)
proto-clean: ## Clean proto artifacts (Java sources and compiled classes)
	@echo "Cleaning proto artifacts..."
	@rm -rf src/java target/classes/cmd target/classes/ser
	@echo "Proto artifacts cleaned"

# Compile existing proto sources (for daily development)
compile: ## Compile existing proto Java sources (does NOT regenerate)
	@if [ ! -d "src/java" ]; then \
		echo "================================================"; \
		echo "ERROR: Proto Java sources not found!"; \
		echo ""; \
		echo "Please run: make proto"; \
		echo ""; \
		echo "This will generate the required proto classes."; \
		echo "================================================"; \
		exit 1; \
	fi
	@echo "Compiling existing proto sources..."
	@clojure -T:build compile-all

# Run tests (checks for proto classes first)
test: ## Run all shared module tests
	@if [ ! -d "target/classes/cmd" ]; then \
		echo "================================================"; \
		echo "ERROR: Proto classes not compiled!"; \
		echo ""; \
		echo "Please run: make proto"; \
		echo ""; \
		echo "This will generate and compile proto classes."; \
		echo "================================================"; \
		exit 1; \
	fi
	@echo "Running shared project tests..."
	@clojure -M:test

# Clean everything EXCEPT proto artifacts
clean: ## Clean build artifacts (preserves proto classes)
	@echo "Cleaning build artifacts (preserving proto classes)..."
	@find target -type f -name "*.class" | grep -v "/cmd/" | grep -v "/ser/" | xargs rm -f 2>/dev/null || true
	@echo "Build artifacts cleaned (proto classes preserved)"

# Full clean including proto artifacts
clean-all: proto-clean ## Clean ALL artifacts including proto classes
	@echo "Cleaning all artifacts..."
	@rm -rf target
	@echo "All artifacts cleaned"