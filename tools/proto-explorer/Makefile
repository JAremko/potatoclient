# Proto Explorer - Protobuf Message Discovery and Inspection Tool
#
# This tool helps you:
# - Find protobuf messages among hundreds of definitions
# - Understand proto to Java class mappings
# - View message structure and field types
# - See Pronto EDN representations for Clojure integration

.DEFAULT_GOAL := help

.PHONY: help
help: ## Show all available commands
	@echo "Proto Explorer - Protobuf Message Discovery Tool"
	@echo "================================================"
	@echo ""
	@echo "Available commands:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
	@echo ""
	@echo "Usage examples:"
	@echo "  make search QUERY=gps     # Find GPS-related messages"
	@echo "  make info CLASS='cmd.JonSharedCmd\$$Root'"
	@echo "  make test                  # Run tests"
	@echo "  make deps-outdated         # Check for outdated dependencies"

.PHONY: search
search: ## Search for protobuf messages (e.g., make search QUERY=gps)
	@if [ -z "$(QUERY)" ]; then \
		echo "Usage: make search QUERY=<search-term>"; \
		echo "Example: make search QUERY=gps"; \
		exit 1; \
	fi
	@clojure -M:run:test-protos search $(QUERY)

.PHONY: list
list: ## List all protobuf messages (optionally filter: make list FILTER=cmd.System)
	@clojure -M:run:test-protos list $(FILTER)

.PHONY: info
info: ## Get info about a protobuf message (e.g., make info CLASS='cmd.JonSharedCmd$$Root')
	@if [ -z "$(CLASS)" ]; then \
		echo "Usage: make info CLASS=<full-class-name>"; \
		echo "Example: make info CLASS='cmd.JonSharedCmd\$$Root'"; \
		echo "Note: Use single quotes for classes with \$$"; \
		exit 1; \
	fi
	@clojure -M:run:test-protos info "$(CLASS)"

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

.PHONY: generate-keyword-tree-cmd
generate-keyword-tree-cmd: ## Generate command keyword tree mapping
	@echo "Generating command keyword tree..."
	@bb generate-keyword-tree-cmd ../../examples/protogen/output/json-descriptors

.PHONY: generate-keyword-tree-state
generate-keyword-tree-state: ## Generate state keyword tree mapping
	@echo "Generating state keyword tree..."
	@bb generate-keyword-tree-state ../../examples/protogen/output/json-descriptors