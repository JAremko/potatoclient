# PotatoClient Build System
# Main project build and development commands

.DEFAULT_GOAL := help

# Variables
JAR_VERSION = $(shell cat VERSION)
JAR_NAME = potatoclient-$(JAR_VERSION).jar
JAR_PATH = target/$(JAR_NAME)

.PHONY: help nrepl dev release clean deps-outdated deps-upgrade test report-unspecced mcp-configure

# Help target
help: ## Show available commands
	@echo "PotatoClient Build System"
	@echo "========================"
	@echo ""
	@echo "Development Commands:"
	@grep -E '^(nrepl|dev|release|clean|recompile):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
	@echo ""
	@echo "Testing:"
	@grep -E '^(test|report-unspecced):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
	@echo ""
	@echo "Dependencies:"
	@grep -E '^(deps-outdated|deps-upgrade):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
	@echo ""
	@echo "Tools:"
	@grep -E '^(mcp-configure):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'

# NREPL for editor integration
nrepl: ensure-compiled ## Start NREPL server on port 7888 for editor integration
	@echo "Starting NREPL server on port 7888..."
	@echo "Connect your editor to localhost:7888"
	clojure -M:nrepl

# Development mode - run from source with full debugging
dev: ensure-compiled ## Run from source with full validation and debugging
	@echo "Starting development mode..."
	@echo "  • Full Malli validation"
	@echo "  • All logging levels enabled"
	@echo "  • Reflection warnings"
	@echo "  • EDN state validation"
	GST_DEBUG=3 clojure -M:run

# Release build - AOT compiled, no debug info
release: ## Build optimized release JAR (AOT, no debug info)
	@echo "Building release version..."
	@echo "  • AOT compilation"
	@echo "  • Metadata stripped"
	@echo "  • WARN/ERROR logging only"
	@echo "  • Direct linking enabled"
	POTATOCLIENT_RELEASE=true clojure -T:build release

# Clean build artifacts
clean: ## Clean all build artifacts
	@echo "Cleaning build artifacts..."
	@rm -rf target/ dist/ .cpcache/ .ktlint/
	@echo "Clean complete"

# Check outdated dependencies
deps-outdated: ## Check for outdated dependencies
	@echo "Checking for outdated dependencies..."
	@clojure -M:outdated

# Upgrade dependencies
deps-upgrade: ## Interactively upgrade dependencies
	@echo "Interactive dependency upgrade..."
	@clojure -M:outdated-upgrade

# Run tests
test: ensure-compiled ## Run tests
	@echo "Running tests..."
	@TEST_RUN_DIR=$$(./scripts/setup-test-logs.sh) && \
	echo "Test output: $$TEST_RUN_DIR" && \
	clojure -M:test 2>&1 | tee "$$TEST_RUN_DIR/test-full.log"

# Report unspecced functions
report-unspecced: ## Report functions missing Malli specs
	@echo "Generating unspecced functions report..."
	@bb scripts/report-unspecced.bb
	@echo "Report saved to ./reports/unspecced-functions.md"

# Configure MCP for Claude
mcp-configure: ## Add MCP server to Claude configuration
	@echo "Adding potatoclient MCP server to Claude..."
	@claude mcp add-json potatoclient '{"command":"/bin/bash","args":["-c","clojure -X:mcp :port 7888"],"env":{}}'
	@echo "MCP server configured"

# Internal targets

# Ensure everything is compiled
.PHONY: ensure-compiled
ensure-compiled:
	@# Compile Java and Kotlin sources if needed
	@if [ ! -d "target/classes/cmd" ] || [ ! -d "target/classes/potatoclient/kotlin" ]; then \
		echo "Compiling Java and Kotlin sources..."; \
		clojure -T:build compile-all; \
	fi

# Force recompile all sources
.PHONY: recompile
recompile: ## Force recompile all Java and Kotlin sources
	@echo "Recompiling all sources..."
	@rm -rf target/classes
	@clojure -T:build compile-all

# Test suites for specific components
test-oneof: ensure-compiled ## Run oneof custom spec tests
	@echo "Running oneof tests..."
	@clojure -M:test-oneof

test-malli: ensure-compiled ## Run malli spec tests
	@echo "Running malli spec tests..."
	@clojure -M:test-malli

test-serialization: ensure-compiled ## Run serialization tests
	@echo "Running serialization tests..."
	@clojure -M:test-serialization

test-cmd: ensure-compiled ## Run command tests
	@echo "Running command tests..."
	@clojure -M:test-cmd

test-ipc: ensure-compiled ## Run IPC tests
	@echo "Running IPC tests..."
	@clojure -M:test-ipc

# Generate clj-kondo configs from Malli specs
kondo-configs: ensure-compiled ## Generate clj-kondo type configs
	@echo "Generating clj-kondo type configs..."
	@clojure -M:kondo-gen
	@echo "Type configs generated"