# PotatoClient Build System
# Main project build and development commands

.DEFAULT_GOAL := help

# Variables
JAR_VERSION = $(shell cat VERSION)
JAR_NAME = potatoclient-$(JAR_VERSION).jar
JAR_PATH = target/$(JAR_NAME)

.PHONY: help nrepl dev release clean fmt lint lint-raw deps-outdated deps-upgrade test report-unspecced mcp-configure

# Help target
help: ## Show available commands
	@echo "PotatoClient Build System"
	@echo "========================"
	@echo ""
	@echo "Development Commands:"
	@grep -E '^(nrepl|dev|release|clean):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
	@echo ""
	@echo "Code Quality:"
	@grep -E '^(fmt|lint|lint-raw|test|report-unspecced):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
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

# Clean main project artifacts only
clean: ## Clean main project build artifacts
	@echo "Cleaning main project artifacts..."
	@rm -rf target/ dist/ .cpcache/
	@echo "Clean complete (shared module preserved)"

# Format code
fmt: ## Format Clojure and Kotlin code
	@echo "Formatting Clojure code..."
	@clojure -M:format fix
	@echo "Formatting Kotlin code..."
	@if [ -f .ktlint/ktlint ]; then \
		.ktlint/ktlint -F "src/potatoclient/kotlin/**/*.kt" || true; \
	else \
		echo "ktlint not found. Installing..."; \
		mkdir -p .ktlint; \
		curl -sSLO https://github.com/pinterest/ktlint/releases/download/1.5.0/ktlint && \
		chmod a+x ktlint && \
		mv ktlint .ktlint/; \
		.ktlint/ktlint -F "src/potatoclient/kotlin/**/*.kt" || true; \
	fi
	@echo "Formatting complete"

# Lint with filtering
lint: ## Run linters and generate filtered report
	@echo "Running linters..."
	@clojure -M:lint --lint src test || true
	@if [ -f .ktlint/ktlint ]; then \
		.ktlint/ktlint "src/potatoclient/kotlin/**/*.kt" || true; \
	fi
	@echo "Generating filtered lint report..."
	@bb scripts/lint-report-filtered.bb
	@echo "Report: reports/lint/summary-filtered.md"

# Lint raw - unfiltered
lint-raw: ## Run linters and generate unfiltered report
	@echo "Running linters (unfiltered)..."
	@clojure -M:lint --lint src test || true
	@if [ -f .ktlint/ktlint ]; then \
		.ktlint/ktlint "src/potatoclient/kotlin/**/*.kt" || true; \
	fi
	@echo "Generating unfiltered lint report..."
	@bb scripts/lint-report.bb
	@echo "Report: reports/lint-report.md"

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
	@# Ensure shared module is compiled
	@if [ ! -d "shared/target/classes/cmd" ] || [ ! -d "shared/target/classes/potatoclient/java/ipc" ]; then \
		echo "Compiling shared module..."; \
		cd shared && $(MAKE) compile; \
	fi
	@# Compile Kotlin sources if needed
	@if [ ! -d "target/classes/potatoclient/kotlin" ]; then \
		clojure -T:build compile-kotlin; \
	fi