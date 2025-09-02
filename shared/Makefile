# Shared Project Makefile
# Central proto generation and compilation for the entire PotatoClient project

.PHONY: proto proto-clean test test-oneof test-malli test-serialization test-cmd compile help deps-outdated deps-upgrade deps-upgrade-all

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
proto: ## Regenerate proto Java sources and compile them (clean rebuild)
	@echo "================================================"
	@echo "Regenerating Proto Classes (Clean Rebuild)"
	@echo "================================================"
	@echo "Cleaning build artifacts..."
	@rm -rf target
	@echo "Generating proto Java sources..."
	@# Script now preserves IPC files and reuses Docker base image
	./scripts/generate-protos.sh || (echo "Proto generation failed, trying fallback..." && \
		mkdir -p src/java && \
		cp -r ../examples/protogen/output/java/* src/java/ 2>/dev/null && \
		echo "Used fallback proto sources from examples/protogen/output/java/")
	@echo "Compiling proto Java sources to bytecode..."
	clojure -T:build compile-proto-sources
	@echo "Compiling Pronto and Clojure sources..."
	clojure -T:build compile-all
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

# Test categories - run specific test suites
test-oneof: ## Run only oneof custom spec tests
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
	@echo "Running oneof tests..."
	@clojure -M:test-oneof

test-malli: ## Run only malli spec tests
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
	@echo "Running malli spec tests..."
	@clojure -M:test-malli

test-serialization: ## Run only serialization/deserialization tests
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
	@echo "Running serialization tests..."
	@clojure -M:test-serialization

test-cmd: ## Run only command tests
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
	@echo "Running command tests..."
	@clojure -M:test-cmd

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

# Check for outdated dependencies
deps-outdated: ## Check for outdated dependencies using antq
	@echo "Checking for outdated dependencies in shared module..."
	@echo "  • Scans deps.edn for newer versions"
	@echo "  • Shows available updates in a table"
	@echo "  • Note: Pronto is pinned to our fork"
	@echo ""
	@cd .. && clojure -M:outdated --directory=shared

# Interactively upgrade dependencies
deps-upgrade: ## Interactively upgrade outdated dependencies
	@echo "Interactive dependency upgrade for shared module..."
	@echo "  • Shows each outdated dependency"
	@echo "  • Lets you choose which to upgrade"
	@echo "  • Automatically updates deps.edn"
	@echo "  • Note: Pronto is pinned to our fork"
	@echo ""
	@cd .. && clojure -M:outdated-upgrade --directory=shared

# Force upgrade all dependencies
deps-upgrade-all: ## Upgrade all outdated dependencies (non-interactive)
	@echo "Upgrading all outdated dependencies in shared module..."
	@echo "  ⚠️  This will automatically update ALL outdated dependencies"
	@echo "  • Updates deps.edn without confirmation"
	@echo "  • Downloads all new versions"
	@echo "  • Remember to test after upgrading!"
	@echo "  • Note: Pronto is pinned to our fork"
	@echo ""
	@read -p "Are you sure? (y/N) " -n 1 -r; \
	echo; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		cd .. && clojure -M:outdated-upgrade --force --directory=shared; \
	else \
		echo "Aborted."; \
	fi

# Generate clj-kondo type configs from Malli specs
kondo-configs: ## Generate clj-kondo type configs from function specs
	@echo "Generating clj-kondo type configs for shared module..."
	@echo "  • Collecting Malli function schemas"
	@echo "  • Extracting type information from Malli specs"
	@echo "  • Writing to .clj-kondo/metosin/malli-types-clj/config.edn"
	@echo ""
	@if [ ! -d "target/classes/cmd" ]; then \
		echo "Proto classes not found. Compiling first..."; \
		$(MAKE) compile; \
	fi
	@cd .. && clojure -M:dev scripts/generate-kondo-configs.clj shared
	@echo ""
	@echo "✓ Type configs generated for shared module"
	@echo "  Location: shared/.clj-kondo/metosin/malli-types-clj/config.edn"