# Proto Explorer - Self-Documenting Build System
# 
# This Makefile integrates with the protogen tool to generate JSON descriptors
# and convert them to Malli specs for protobuf validation.
#
# Key Features:
# - Generates Malli specs with buf.validate constraint support
# - Provides constraint-aware test data generation
# - Supports both REPL and CLI interfaces
#
# Default target shows help
.DEFAULT_GOAL := help

# Variables
PROTOGEN_DIR = ../../examples/protogen
PROTO_DIR = proto
OUTPUT_DIR = output
JSON_DESCRIPTORS_DIR = $(OUTPUT_DIR)/json-descriptors
JAVA_DIR = $(OUTPUT_DIR)/java
SPECS_DIR = ../../shared/specs/protobuf
REPORTS_DIR = reports

# Colors for output
GREEN := \033[0;32m
YELLOW := \033[1;33m
NC := \033[0m # No Color

# Help target
.PHONY: help
help: ## Show all available commands with detailed descriptions
	@echo "Proto Explorer Build System"
	@echo "=========================="
	@echo ""
	@echo "This tool generates Malli specs from protobuf definitions using JSON descriptors."
	@echo ""
	@echo "MAIN COMMANDS:"
	@grep -E '^(generate-all|clean|test):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
	@echo ""
	@echo "GENERATION STEPS:"
	@grep -E '^(copy-protos|generate-descriptors|generate-specs):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
	@echo ""
	@echo "DEVELOPMENT:"
	@grep -E '^(repl|test-json-parser|analyze-descriptors):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
	@echo ""
	@echo "CLI COMMANDS:"
	@grep -E '^(cli-|bb-):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
	@echo ""
	@echo "For detailed documentation, see README.md and TODO.md"

###############################################################################
# MAIN COMMANDS
###############################################################################

.PHONY: proto
proto: copy-protos generate-descriptors ## Alias for generating proto files and JSON descriptors

.PHONY: generate-all
generate-all: proto generate-specs ## Complete generation pipeline: protos → JSON → specs

.PHONY: clean
clean: ## Remove all generated files and outputs
	@echo "$(YELLOW)Cleaning generated files...$(NC)"
	@rm -rf $(OUTPUT_DIR)
	@rm -rf $(PROTO_DIR)
	@rm -rf $(REPORTS_DIR)
	@echo "$(GREEN)Clean complete$(NC)"

.PHONY: test
test: ## Run all tests
	@echo "$(GREEN)Running tests...$(NC)"
	@clojure -M:test

###############################################################################
# GENERATION STEPS
###############################################################################

.PHONY: copy-protos
copy-protos: ## Copy proto files from protogen repository
	@echo "$(GREEN)Copying proto files from protogen...$(NC)"
	@if [ ! -d "$(PROTOGEN_DIR)/proto" ]; then \
		echo "$(YELLOW)Error: Protogen proto directory not found at $(PROTOGEN_DIR)/proto$(NC)"; \
		echo "Make sure you have cloned the protogen repository"; \
		exit 1; \
	fi
	@mkdir -p $(PROTO_DIR)
	@cp -r $(PROTOGEN_DIR)/proto/* $(PROTO_DIR)/
	@echo "$(GREEN)Proto files copied to $(PROTO_DIR)$(NC)"

.PHONY: generate-descriptors
generate-descriptors: copy-protos ## Generate JSON descriptors using protogen
	@echo "$(GREEN)Generating JSON descriptors...$(NC)"
	@cd $(PROTOGEN_DIR) && \
		PROTO_SOURCE_DIR=$(shell pwd)/$(PROTO_DIR) \
		OUTPUT_BASE_DIR=$(shell pwd)/$(OUTPUT_DIR) \
		make generate
	@echo "$(GREEN)JSON descriptors generated in $(JSON_DESCRIPTORS_DIR)$(NC)"

.PHONY: generate-specs
generate-specs: ## Generate Malli specs from JSON descriptors
	@echo "$(GREEN)Generating Malli specs from JSON descriptors...$(NC)"
	@mkdir -p $(SPECS_DIR)
	@clojure -M:run generate-specs \
		--input $(JSON_DESCRIPTORS_DIR) \
		--output $(SPECS_DIR) \
		--verbose
	@echo "$(GREEN)Malli specs generated in $(SPECS_DIR)$(NC)"

###############################################################################
# DEVELOPMENT COMMANDS
###############################################################################

.PHONY: repl
repl: ## Start a REPL for interactive development
	@echo "$(GREEN)Starting REPL...$(NC)"
	@clojure -M:repl

.PHONY: nrepl
nrepl: ## Start nREPL server on port 7889
	@echo "$(GREEN)Starting nREPL server on port 7889...$(NC)"
	@clojure -M:nrepl

.PHONY: mcp-server
mcp-server: ## Start nREPL server for MCP integration
	@echo "$(GREEN)Starting Proto Explorer nREPL server on port 7889...$(NC)"
	@echo ""
	@echo "Once started, in another terminal run:"
	@echo "  clojure -X:mcp :port 7889"
	@echo ""
	@echo "Then in Claude, use:"
	@echo "  claude mcp add proto-explorer stdio \"cd $(shell pwd) && clojure -X:mcp :port 7889\""
	@clojure -M:nrepl

.PHONY: mcp-configure
mcp-configure: ## Configure Proto Explorer as MCP server in Claude
	@echo "$(GREEN)Configuring Proto Explorer as MCP server...$(NC)"
	@echo "Running: claude mcp add proto-explorer stdio \"cd $(shell pwd) && clojure -X:mcp\""
	@claude mcp add proto-explorer stdio "cd $(shell pwd) && clojure -X:mcp"
	@echo "$(GREEN)Done! Proto Explorer is now available in Claude.$(NC)"

.PHONY: test-json-parser
test-json-parser: ## Test JSON descriptor parsing
	@echo "$(GREEN)Testing JSON descriptor parser...$(NC)"
	@if [ -f "$(JSON_DESCRIPTORS_DIR)/descriptor-set.json" ]; then \
		clojure -M:run analyze-json \
			--input $(JSON_DESCRIPTORS_DIR)/descriptor-set.json; \
	else \
		echo "$(YELLOW)No JSON descriptors found. Run 'make generate-descriptors' first.$(NC)"; \
		exit 1; \
	fi

.PHONY: analyze-descriptors
analyze-descriptors: ## Analyze generated JSON descriptors
	@echo "$(GREEN)Analyzing JSON descriptors...$(NC)"
	@if [ -d "$(JSON_DESCRIPTORS_DIR)" ]; then \
		echo "Files in $(JSON_DESCRIPTORS_DIR):"; \
		ls -la $(JSON_DESCRIPTORS_DIR)/*.json | head -10; \
		echo ""; \
		echo "Total JSON files: $$(ls $(JSON_DESCRIPTORS_DIR)/*.json | wc -l)"; \
		echo "Total size: $$(du -sh $(JSON_DESCRIPTORS_DIR) | cut -f1)"; \
	else \
		echo "$(YELLOW)No JSON descriptors found. Run 'make generate-descriptors' first.$(NC)"; \
		exit 1; \
	fi

###############################################################################
# UTILITY COMMANDS
###############################################################################

.PHONY: validate-protogen
validate-protogen: ## Check if protogen is properly set up
	@echo "$(GREEN)Validating protogen setup...$(NC)"
	@if [ ! -d "$(PROTOGEN_DIR)" ]; then \
		echo "$(YELLOW)Error: Protogen not found at $(PROTOGEN_DIR)$(NC)"; \
		echo "Please ensure protogen is cloned at the correct location"; \
		exit 1; \
	fi
	@if [ ! -f "$(PROTOGEN_DIR)/Makefile" ]; then \
		echo "$(YELLOW)Error: Protogen Makefile not found$(NC)"; \
		exit 1; \
	fi
	@echo "$(GREEN)Protogen setup is valid$(NC)"

.PHONY: quick-test
quick-test: ## Run quick tests with test proto files
	@echo "$(GREEN)Running quick test with test protos...$(NC)"
	@mkdir -p test-output
	@clojure -M:test-quick

.PHONY: coverage
coverage: ## Generate test coverage report
	@echo "$(GREEN)Generating coverage report...$(NC)"
	@clojure -M:test:coverage

.PHONY: lint
lint: ## Run linters on the codebase
	@echo "$(GREEN)Running linters...$(NC)"
	@clojure -M:lint

.PHONY: format
format: ## Format code using cljfmt
	@echo "$(GREEN)Formatting code...$(NC)"
	@clojure -M:format

###############################################################################
# REPORTING COMMANDS
###############################################################################

.PHONY: report-specs
report-specs: ## Generate a report of all generated specs
	@echo "$(GREEN)Generating spec report...$(NC)"
	@mkdir -p $(REPORTS_DIR)
	@clojure -M:run report-specs \
		--input $(SPECS_DIR) \
		--output $(REPORTS_DIR)/spec-report.md

.PHONY: report-validation
report-validation: ## Report on buf.validate constraint coverage
	@echo "$(GREEN)Generating validation report...$(NC)"
	@mkdir -p $(REPORTS_DIR)
	@clojure -M:run report-validation \
		--input $(JSON_DESCRIPTORS_DIR) \
		--output $(REPORTS_DIR)/validation-report.md

###############################################################################
# CLI COMMANDS
###############################################################################

.PHONY: cli-find
cli-find: ## Find specs by pattern using CLI (usage: make cli-find PATTERN=rotary)
	@bb find $(PATTERN)

.PHONY: cli-spec
cli-spec: ## Get spec definition using CLI (usage: make cli-spec SPEC=:cmd/ping)
	@bb spec $(SPEC)

.PHONY: cli-example
cli-example: ## Generate example data using CLI (usage: make cli-example SPEC=:cmd/ping)
	@bb example $(SPEC)

.PHONY: cli-examples
cli-examples: ## Generate multiple examples using CLI (usage: make cli-examples SPEC=:cmd/ping N=5)
	@bb examples $(SPEC) $(N)

.PHONY: cli-list
cli-list: ## List all messages using CLI (usage: make cli-list [PACKAGE=cmd])
	@bb list $(PACKAGE)

.PHONY: java-class
java-class: ## Get Java class info (usage: make java-class MSG=Root)
	@clojure -M:run java-class $(MSG)

.PHONY: java-fields
java-fields: ## Get proto field mapping (usage: make java-fields MSG=Root)
	@clojure -M:run java-fields $(MSG)

.PHONY: java-builder
java-builder: ## Get Java builder info (usage: make java-builder MSG=Root)
	@clojure -M:run java-builder $(MSG)

.PHONY: java-summary
java-summary: ## Get Java class summary (usage: make java-summary MSG=Root)
	@clojure -M:run java-summary $(MSG)

.PHONY: cli-stats
cli-stats: ## Show spec statistics using CLI
	@bb stats

.PHONY: bb-install
bb-install: ## Install Babashka if not already installed
	@if ! command -v bb &> /dev/null; then \
		echo "$(YELLOW)Installing Babashka...$(NC)"; \
		curl -sLO https://raw.githubusercontent.com/babashka/babashka/master/install && \
		bash install && \
		rm install; \
	else \
		echo "$(GREEN)Babashka already installed$(NC)"; \
	fi

###############################################################################
# BUILD COMMANDS
###############################################################################

.PHONY: uberjar
uberjar: ## Build uberjar for proto-explorer (includes Java reflection features)
	@echo "$(GREEN)Building uberjar...$(NC)"
	@clojure -T:build uberjar

###############################################################################
# CI/CD COMMANDS
###############################################################################

.PHONY: ci-test
ci-test: clean generate-all test ## Full CI test suite

.PHONY: ci-validate
ci-validate: generate-all report-validation ## Validate constraint coverage

.PHONY: install-git-hooks
install-git-hooks: ## Install git hooks for pre-commit checks
	@echo "$(GREEN)Installing git hooks...$(NC)"
	@ln -sf ../../scripts/pre-commit.sh .git/hooks/pre-commit
	@chmod +x .git/hooks/pre-commit
	@echo "$(GREEN)Git hooks installed$(NC)"

# Ensure protogen is available before main operations
generate-descriptors: validate-protogen
copy-protos: validate-protogen