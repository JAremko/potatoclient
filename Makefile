# PotatoClient - Self-Documenting Build System
# 
# This Makefile provides all build, development, and quality assurance commands
# with comprehensive inline documentation. Run 'make help' for a complete overview.
#
# For detailed project documentation, see:
# - CLAUDE.md - Main developer guide
# - .claude/kotlin-subprocess.md - Kotlin architecture details
# - .claude/protobuf-command-system.md - Command system documentation
#
# Default target shows help
.DEFAULT_GOAL := help

# Variables
# Version is read from VERSION file
JAR_VERSION = $(shell cat VERSION)
JAR_NAME = potatoclient-$(JAR_VERSION).jar
JAR_PATH = target/$(JAR_NAME)

# Help target
.PHONY: help
help: ## Show all available commands with detailed descriptions
	@echo "PotatoClient Build System"
	@echo "========================="
	@echo ""
	@echo "This Makefile is self-documenting. Each target includes detailed"
	@echo "information about its purpose, features, and behavior."
	@echo ""
	@echo "PRIMARY DEVELOPMENT COMMANDS:"
	@grep -E '^(dev|nrepl|run):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
	@echo ""
	@echo "BUILD & RELEASE:"
	@grep -E '^(build|release|proto|compile-kotlin|compile-java-proto|clean|rebuild):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
	@echo ""
	@echo "CODE QUALITY:"
	@grep -E '^(fmt|lint|lint-raw):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
	@echo ""
	@echo "OTHER TOOLS:"
	@grep -E '^(test|report-unspecced|check-deps|build-macos-dev):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
	@echo ""
	@echo "MCP SERVER (Claude integration):"
	@grep -E '^(mcp-server|mcp-configure):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
	@echo ""
	@echo "DOCUMENTATION:"
	@echo "  - Project guide: CLAUDE.md"
	@echo "  - Kotlin subprocess: .claude/kotlin-subprocess.md"
	@echo "  - Command system: .claude/protobuf-command-system.md"
	@echo "  - Custom lint reports: bb scripts/lint-report.bb --help"
	@echo ""
	@echo "For detailed documentation, see CLAUDE.md"
	@echo "For custom lint report options, see: bb scripts/lint-report.bb --help"

###############################################################################
# BUILD TARGETS
###############################################################################

# Build proto files
.PHONY: proto
proto: ## Regenerate protobuf classes from protogen repository - always fresh
	@echo "Generating Java classes from proto files using Protogen Docker..."
	@echo "  • Clones latest protogen repository with bundled proto definitions"
	@echo "  • Imports pre-built base image from Git LFS (or builds from scratch)"
	@echo "  • Builds Docker image with all proto files included"
	@echo "  • Generates Java bindings with buf.validate annotations preserved"
	@echo "  • Custom kebab-case conversion for Clojure idioms"
	@echo "  • Cleans up Docker images after generation to save space"
	./scripts/generate-protos.sh

# Compile Kotlin sources
.PHONY: compile-kotlin
compile-kotlin: ## Compile Kotlin source files
	@echo "Compiling Kotlin sources..."
	clojure -T:build compile-kotlin

# Compile Java protobuf sources
.PHONY: compile-java-proto
compile-java-proto: ## Compile Java protobuf source files
	@echo "Compiling Java protobuf sources..."
	clojure -T:build compile-java-proto

# Build target
.PHONY: build
build: proto compile-kotlin compile-java-proto ## Build the project (creates JAR file)
	@echo "Building project..."
	clojure -T:build uber

# Release build target
.PHONY: release
release: ## Build optimized JAR for distribution (stripped metadata, WARN/ERROR logging only, AOT compiled)
	@echo "Building release version..."
	@echo "  ✓ Guardrails completely removed from bytecode"
	@echo "  ✓ Only WARN/ERROR logging to platform-specific locations"
	@echo "  ✓ Window title shows [RELEASE]"
	@echo "  ✓ Metadata stripped (smaller JAR)"
	@echo "  ✓ AOT compilation and direct linking"
	@echo "  ✓ Release JARs auto-detect they're release builds"
	@echo ""
	POTATOCLIENT_RELEASE=true clojure -T:build release


# Clean proto files
.PHONY: clean-proto
clean-proto: ## Clean generated proto Java files
	@echo "Cleaning generated proto files..."
	rm -rf src/potatoclient/java/ser/
	rm -rf src/potatoclient/java/cmd/
	rm -rf src/potatoclient/java/jon/
	rm -rf src/potatoclient/java/com/
	rm -rf src/potatoclient/java/build/

# Clean target
.PHONY: clean
clean: clean-proto clean-cache ## Clean all build artifacts
	@echo "Cleaning build artifacts..."
	clojure -T:build clean
	rm -rf target/
	rm -rf dist/

# NREPL target
.PHONY: nrepl
nrepl: proto compile-kotlin compile-java-proto ## REPL on port 7888 for interactive development (same validation features as make dev)
	@echo "Starting NREPL server on port 7888..."
	@echo "  ✓ Full Guardrails validation"
	@echo "  ✓ All logging levels enabled"
	@echo "  ✓ Connect your editor to port 7888"
	@echo ""
	clojure -M:nrepl

# MCP Server target
.PHONY: mcp-server
mcp-server: ## Start MCP server on port 7888 (for Claude integration)
	@echo "Starting Clojure MCP server on port 7888..."
	@echo ""
	@echo "This server allows Claude to interact with your Clojure project."
	@echo "Keep this terminal open while using Claude."
	@echo ""
	@echo "To use in Claude:"
	@echo "  1. Keep this server running"
	@echo "  2. In Claude, use the /mcp command to connect"
	@echo ""
	clojure -X:mcp :port 7888

# Configure MCP in Claude
.PHONY: mcp-configure
mcp-configure: ## Add potatoclient MCP server to Claude configuration
	@echo "Adding potatoclient MCP server to Claude..."
	claude mcp add-json potatoclient '{"command":"/bin/bash","args":["-c","clojure -X:mcp :port 7888"],"env":{}}'
	@echo "✓ MCP server configuration added to Claude"

# Test target
.PHONY: test
test: build ## Run tests
	@echo "Running tests..."
	clojure -M:test

# Report unspecced functions
.PHONY: report-unspecced
report-unspecced: proto compile-kotlin compile-java-proto ## Check which functions need Guardrails specs - mandatory for all functions
	@echo "Generating unspecced functions report..."
	@echo "  • Lists all functions that lack Malli instrumentation"
	@echo "  • Groups them by namespace"
	@echo "  • Provides statistics on coverage"
	@echo "  • Remember: Use >defn and >defn- for ALL functions (never raw defn)"
	@echo ""
	clojure -M:run --report-unspecced
	@echo ""
	@echo "Report saved to ./reports/unspecced-functions.md"

# Check dependencies
.PHONY: check-deps
check-deps: ## Check that required tools are installed
	@echo "Checking dependencies..."
	@command -v clojure >/dev/null 2>&1 || { echo "Error: clojure not found. Please install Clojure CLI."; exit 1; }
	@command -v java >/dev/null 2>&1 || { echo "Error: java not found. Please install Java."; exit 1; }
	@echo "All dependencies found!"

# Quick rebuild
.PHONY: rebuild
rebuild: clean build ## Clean and rebuild the project

###############################################################################
# PRIMARY DEVELOPMENT COMMANDS
###############################################################################

# Development target - runs with all validation, logging, and debugging features
.PHONY: dev
dev: clean-cache proto compile-kotlin compile-java-proto ## PRIMARY DEVELOPMENT COMMAND - Full validation, all logs, warnings (takes 30-40s to start)
	@echo "Running development version with:"
	@echo "  ✓ Full Guardrails validation catches bugs immediately"
	@echo "  ✓ Reflection warnings for performance issues"
	@echo "  ✓ All log levels (DEBUG, INFO, WARN, ERROR) to console and ./logs/"
	@echo "  ✓ GStreamer debug output (GST_DEBUG=3)"
	@echo "  ✓ Window title shows [DEVELOPMENT]"
	@echo "  ✓ Running from source (no AOT compilation)"
	@echo ""
	@echo "⚠️  Takes 30-40 seconds to start - set appropriate timeouts in your tools!"
	@echo ""
	GST_DEBUG=3 clojure -M:run

# Run the JAR file in production-like mode
.PHONY: run
run: build ## Test the JAR in production-like mode (Guardrails disabled, near-production speed)
	@echo "Running JAR in production-like mode..."
	@echo "  ✓ Guardrails disabled"
	@echo "  ✓ No reflection warnings"
	@echo "  ✓ Standard logging levels"
	@echo "  ✓ Window title shows [DEVELOPMENT]"
	@echo "  ✓ Near-production performance"
	@echo ""
	java -jar $(JAR_PATH)

# Clean cache target
.PHONY: clean-cache
clean-cache: ## Clean Clojure compilation cache to ensure fresh code runs
	@echo "Cleaning compilation cache..."
	@rm -rf .cpcache/
	@rm -rf target/classes/
	@find . -name "*.class" -type f -delete 2>/dev/null || true

# Build for macOS development (unsigned, uses system Java)
.PHONY: build-macos-dev
build-macos-dev: ## Build unsigned macOS .app bundle for development
	@echo "Building macOS development version..."
	./scripts/build-macos-dev.sh

###############################################################################
# CODE QUALITY & LINTING
###############################################################################

# Format Clojure code
.PHONY: fmt-clojure
fmt-clojure: ## Format all Clojure code using cljfmt
	@echo "Formatting Clojure code..."
	clojure -M:format fix
	@echo "✓ Clojure formatting complete"

# Format Kotlin code
.PHONY: fmt-kotlin
fmt-kotlin: ## Format all Kotlin code using ktlint (JetBrains IDEA compatible)
	@echo "Formatting Kotlin code..."
	@if [ -f .ktlint/ktlint ]; then \
		.ktlint/ktlint -F "src/potatoclient/kotlin/**/*.kt" || exit 1; \
	else \
		echo "ktlint not found. Installing..."; \
		mkdir -p .ktlint; \
		curl -sSLO https://github.com/pinterest/ktlint/releases/download/1.5.0/ktlint && \
		chmod a+x ktlint && \
		mv ktlint .ktlint/; \
		.ktlint/ktlint -F "src/potatoclient/kotlin/**/*.kt" || exit 1; \
	fi
	@echo "✓ Kotlin formatting complete"

# Format all code
.PHONY: fmt
fmt: ## Format all Clojure and Kotlin code
	@echo "Formatting all code..."
	@$(MAKE) -s fmt-clojure
	@$(MAKE) -s fmt-kotlin
	@echo "✓ All code formatting complete"

# Check Clojure formatting
.PHONY: fmt-check-clojure
fmt-check-clojure: ## Check if Clojure code is properly formatted
	@echo "Checking Clojure code formatting..."
	@if clojure -M:format check 2>&1 | grep -q "files formatted incorrectly"; then \
		echo "❌ Clojure formatting issues found!"; \
		echo "Run 'make fmt' or 'make fmt-clojure' to fix them."; \
		clojure -M:format check; \
		exit 1; \
	else \
		echo "✓ All Clojure files are properly formatted"; \
	fi

# Check Kotlin formatting
.PHONY: fmt-check-kotlin
fmt-check-kotlin: ## Check if Kotlin code is properly formatted
	@echo "Checking Kotlin code formatting..."
	@if [ -f .ktlint/ktlint ]; then \
		if ! .ktlint/ktlint "src/potatoclient/kotlin/**/*.kt" > /dev/null 2>&1; then \
			echo "❌ Kotlin formatting issues found!"; \
			echo "Run 'make fmt' or 'make fmt-kotlin' to fix them."; \
			.ktlint/ktlint "src/potatoclient/kotlin/**/*.kt"; \
			exit 1; \
		else \
			echo "✓ All Kotlin files are properly formatted"; \
		fi \
	else \
		echo "ktlint not found. Installing..."; \
		mkdir -p .ktlint; \
		curl -sSLO https://github.com/pinterest/ktlint/releases/download/1.5.0/ktlint && \
		chmod a+x ktlint && \
		mv ktlint .ktlint/; \
		if ! .ktlint/ktlint "src/potatoclient/kotlin/**/*.kt" > /dev/null 2>&1; then \
			echo "❌ Kotlin formatting issues found!"; \
			echo "Run 'make fmt' or 'make fmt-kotlin' to fix them."; \
			.ktlint/ktlint "src/potatoclient/kotlin/**/*.kt"; \
			exit 1; \
		else \
			echo "✓ All Kotlin files are properly formatted"; \
		fi \
	fi

# Check all formatting
.PHONY: fmt-check
fmt-check:
	@echo "Checking all code formatting..."
	@$(MAKE) -s fmt-check-clojure
	@$(MAKE) -s fmt-check-kotlin
	@echo "✓ All code is properly formatted"

# Lint Clojure code with clj-kondo
.PHONY: lint-clj
lint-clj:
	@echo "Linting Clojure code with clj-kondo..."
	@echo "  • Guardrails support: >defn, >defn-, >def properly recognized"
	@echo "  • Seesaw macros: UI construction functions linted correctly"
	@echo "  • Telemere logging: Log macros understood without false positives"
	@clojure -M:lint --lint src test || exit 1
	@echo "✓ Clojure linting complete"

# Lint Kotlin code with ktlint
.PHONY: lint-kotlin
lint-kotlin:
	@echo "Linting Kotlin code with ktlint..."
	@if [ -f .ktlint/ktlint ]; then \
		.ktlint/ktlint "src/potatoclient/kotlin/**/*.kt" || exit 1; \
	else \
		echo "ktlint not found. Installing..."; \
		mkdir -p .ktlint; \
		curl -sSLO https://github.com/pinterest/ktlint/releases/download/1.5.0/ktlint && \
		chmod a+x ktlint && \
		mv ktlint .ktlint/; \
		.ktlint/ktlint "src/potatoclient/kotlin/**/*.kt" || exit 1; \
	fi
	@echo "✓ Kotlin linting complete"

# Run detekt for advanced Kotlin static analysis
.PHONY: lint-kotlin-detekt
lint-kotlin-detekt:
	@echo "Running detekt for advanced Kotlin static analysis..."
	@echo "  • Complexity thresholds (cognitive and cyclomatic)"
	@echo "  • Exception handling patterns"
	@echo "  • Performance anti-patterns"
	@echo "  • Code smell detection"
	@if [ -f .detekt/detekt-cli.jar ]; then \
		java -jar .detekt/detekt-cli.jar \
			--config detekt.yml \
			--input src/potatoclient/kotlin \
			--report html:reports/detekt.html \
			--report txt:reports/detekt.txt || exit 1; \
	else \
		echo "detekt not found. Installing..."; \
		mkdir -p .detekt reports; \
		curl -sSL https://github.com/detekt/detekt/releases/download/v1.23.7/detekt-cli-1.23.7-all.jar -o .detekt/detekt-cli.jar && \
		java -jar .detekt/detekt-cli.jar \
			--config detekt.yml \
			--input src/potatoclient/kotlin \
			--report html:reports/detekt.html \
			--report txt:reports/detekt.txt || exit 1; \
	fi
	@echo "✓ Detekt analysis complete. Reports saved to reports/detekt.*"

# Run all linters and generate filtered report
.PHONY: lint
lint: fmt-check ## Run all linters and generate filtered report (reports/lint/)
	@echo "Running all linters..."
	@$(MAKE) -s lint-clj || true
	@$(MAKE) -s lint-kotlin || true
	@$(MAKE) -s lint-kotlin-detekt || true
	@echo "Generating filtered lint report..."
	@echo "  • Filters Seesaw UI patterns (:text, :items, :border)"
	@echo "  • Filters Telemere functions (handler:console)"
	@echo "  • Filters Guardrails symbols (>, |, ?, =>)"
	@echo "  • Filters standard library false warnings"
	@bb scripts/lint-report-filtered.bb
	@echo "✓ Filtered reports generated in reports/lint/"
	@echo "  • Summary: reports/lint/summary-filtered.md"
	@echo "  • Real issues: reports/lint/clojure-real.md"
	@echo "  • Full analysis: reports/lint/"

# Generate raw (unfiltered) lint report
.PHONY: lint-raw
lint-raw: fmt-check ## Run all linters and generate full unfiltered report (reports/)
	@echo "Running all linters..."
	@$(MAKE) -s lint-clj || true
	@$(MAKE) -s lint-kotlin || true
	@$(MAKE) -s lint-kotlin-detekt || true
	@echo "Generating comprehensive lint report..."
	@echo "  • Summary of total errors and warnings"
	@echo "  • Breakdown by linter (clj-kondo, ktlint, detekt)"
	@echo "  • Issues organized by file with line numbers"
	@echo "  • Specific rule violations and descriptions"
	@bb scripts/lint-report.bb
	@echo "✓ Full unfiltered report generated:"
	@echo "  • Main report: reports/lint-report.md"
	@echo "  • Detailed reports: reports/lint/"
	@echo "  • Detekt reports: reports/detekt.html, reports/detekt.txt"

.PHONY: all
all: clean build ## Clean and build everything
