# Proto Explorer - Protobuf Message Discovery and Inspection Tool
#
# This tool helps you:
# - Find protobuf messages among hundreds of definitions
# - Understand proto to Java class mappings
# - View message structure and field types
# - See Pronto EDN representations for Clojure integration

.DEFAULT_GOAL := help

.PHONY: help
help: ## Show 2-step workflow
	@echo "Proto Explorer - 2-Step Workflow"
	@echo "================================="
	@echo ""
	@echo "STEP 1: Search for messages"
	@echo "  make search QUERY=<term>       # Search by name or Java class"
	@echo "  make list [FILTER=<package>]   # List all messages"
	@echo ""
	@echo "STEP 2: Get details"
	@echo "  make info QUERY='<result>'     # Use query string from step 1"
	@echo ""
	@echo "Examples:"
	@echo "  make search QUERY=root"
	@echo "  make info QUERY='cmd.JonSharedCmd\$$Root'"
	@echo ""
	@echo "Development:"
	@echo "  make test                       # Run tests"
	@echo "  make repl                       # Start REPL"

# Ensure proto classes are compiled
.PHONY: ensure-proto-classes
ensure-proto-classes:
	@if [ ! -d "../../target/classes/ser" ] || [ ! -d "../../target/classes/cmd" ]; then \
		echo "Proto classes missing, compiling..."; \
		cd ../.. && make compile-java-proto; \
	else \
		echo "Proto classes already compiled"; \
	fi

.PHONY: search
search: ensure-proto-classes ## [STEP 1] Search for protobuf messages
	@if [ -z "$(QUERY)" ]; then \
		echo "Usage: make search QUERY=<search-term>"; \
		echo ""; \
		echo "Examples:"; \
		echo "  make search QUERY=root         # Find all Root messages"; \
		echo "  make search QUERY=gps          # Find GPS-related messages"; \
		echo "  make search QUERY=JonSharedCmd # Find by Java class"; \
		exit 1; \
	fi
	@clojure -M:run:test-protos search $(QUERY)

.PHONY: list
list: ensure-proto-classes ## [STEP 1] List all protobuf messages
	@clojure -M:run:test-protos list $(FILTER)

.PHONY: info
info: ensure-proto-classes ## [STEP 2] Get detailed info using query from step 1
	@if [ -z "$(QUERY)" ]; then \
		echo "Usage: make info QUERY='<result-from-search>'"; \
		echo ""; \
		echo "Use the query string shown in search results."; \
		echo "Example: make info QUERY='cmd.JonSharedCmd\$$Root'"; \
		exit 1; \
	fi
	@clojure -M:run:test-protos info "$(QUERY)"

.PHONY: repl
repl: ## Start a REPL for interactive exploration
	@echo "Starting REPL on port 7889..."
	@clojure -M:nrepl

.PHONY: test
test: ## Run all tests
	@echo "Running tests..."
	@clojure -M:test

.PHONY: test-unit
test-unit: ## Run unit tests only
	@echo "Running unit tests..."
	@clojure -M:test:unit

.PHONY: test-integration
test-integration: ## Run integration tests only
	@echo "Running integration tests..."
	@clojure -M:test:integration

.PHONY: deps-outdated
deps-outdated: ## Check for outdated dependencies
	@echo "Checking for outdated dependencies..."
	@echo "Note: Excluding pronto (git dependency) from checks"
	@clojure -Sdeps '{:aliases {:outdated {:extra-deps {com.github.liquidz/antq {:mvn/version "2.7.1133"}} :main-opts ["-m" "antq.core" "--exclude=pronto/pronto"]}}}' -M:outdated

.PHONY: deps-upgrade
deps-upgrade: ## Interactively upgrade dependencies (excludes pronto)
	@echo "Interactive dependency upgrade..."
	@echo "Note: pronto (git dependency) is excluded from upgrades"
	@clojure -Sdeps '{:aliases {:outdated {:extra-deps {com.github.liquidz/antq {:mvn/version "2.7.1133"}} :main-opts ["-m" "antq.core" "--exclude=pronto/pronto" "--upgrade"]}}}' -M:outdated

.PHONY: format
format: ## Format code using cljfmt
	@echo "Formatting code..."
	@clojure -M:fmt fix

.PHONY: format-check
format-check: ## Check code formatting
	@echo "Checking code format..."
	@clojure -M:fmt check

.PHONY: clean
clean: ## Clean build artifacts
	@echo "Cleaning..."
	@rm -rf target/ .cpcache/

