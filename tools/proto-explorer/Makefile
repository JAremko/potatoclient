# Proto Explorer - Protobuf Message Discovery and Inspection Tool
#
# This tool helps you:
# - Find protobuf messages among hundreds of definitions
# - Understand proto to Java class mappings
# - View message structure and field types
# - See buf.validate constraints for field validation
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

# Proto classes are now provided by the shared project dependency

.PHONY: search proto-search
search proto-search: ## [STEP 1] Search for protobuf messages
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

.PHONY: list proto-list
list proto-list: ensure-proto-classes ## [STEP 1] List all protobuf messages
	@clojure -M:run:test-protos list $(FILTER)

.PHONY: info proto-info
info proto-info: ensure-proto-classes ## [STEP 2] Get detailed info using query from step 1
	@if [ -z "$(QUERY)" ]; then \
		echo "Usage: make info QUERY='<result-from-search>'"; \
		echo ""; \
		echo "Use the query string shown in search results."; \
		echo "Example: make info QUERY='cmd.JonSharedCmd\$$Root'"; \
		exit 1; \
	fi
	@clojure -M:run:test-protos info "$(QUERY)"

.PHONY: repl proto-repl
repl proto-repl: ## Start a REPL for interactive exploration
	@echo "Starting REPL on port 7889..."
	@clojure -M:nrepl

.PHONY: test proto-test
test proto-test: ## Run all tests
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
deps-outdated: ## Check for outdated dependencies using antq
	@echo "Checking for outdated dependencies..."
	@echo "  • Scans deps.edn for newer versions"
	@echo "  • Shows available updates in a table"
	@echo "  • Includes both direct and transitive dependencies"
	@echo "  • Note: Pronto is pinned to our fork"
	@echo ""
	@cd ../.. && clojure -M:outdated --directory=tools/proto-explorer

.PHONY: deps-upgrade
deps-upgrade: ## Interactively upgrade outdated dependencies
	@echo "Interactive dependency upgrade..."
	@echo "  • Shows each outdated dependency"
	@echo "  • Lets you choose which to upgrade"
	@echo "  • Automatically updates deps.edn"
	@echo "  • Downloads new dependencies"
	@echo "  • Note: Pronto is pinned to our fork"
	@echo ""
	@cd ../.. && clojure -M:outdated-upgrade --directory=tools/proto-explorer

.PHONY: deps-upgrade-all
deps-upgrade-all: ## Upgrade all outdated dependencies (non-interactive)
	@echo "Upgrading all outdated dependencies..."
	@echo "  ⚠️  This will automatically update ALL outdated dependencies"
	@echo "  • Updates deps.edn without confirmation"
	@echo "  • Downloads all new versions"
	@echo "  • Remember to test after upgrading!"
	@echo "  • Note: Pronto is pinned to our fork"
	@echo ""
	@read -p "Are you sure? (y/N) " -n 1 -r; \
	echo ""; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		cd ../.. && clojure -M:outdated-upgrade --force --directory=tools/proto-explorer; \
		echo "✓ Dependencies upgraded. Remember to run 'make test' to verify!"; \
	else \
		echo "Upgrade cancelled."; \
	fi

.PHONY: format
format: ## Format code using cljfmt
	@echo "Formatting code..."
	@clojure -M:fmt fix

.PHONY: format-check
format-check: ## Check code formatting
	@echo "Checking code format..."
	@clojure -M:fmt check

.PHONY: clean proto-clean
clean proto-clean: ## Clean build artifacts
	@echo "Cleaning..."
	@rm -rf target/ .cpcache/

# Catch-all target for unknown commands
%:
	@echo "Error: Unknown target '$@'"
	@echo ""
	@$(MAKE) help

