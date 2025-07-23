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
	@grep -E '^(lint|lint-clj|lint-kotlin|lint-kotlin-detekt|fmt|fmt-check):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
	@echo ""
	@echo "LINT REPORTS:"
	@grep -E '^(lint-report|lint-report-filtered|lint-report-warnings):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
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
proto: ## Regenerate protobuf classes (if .proto files change) - custom kebab-case conversion
	@echo "Generating Java classes from proto files..."
	@echo "  • Direct protobuf implementation (no external wrapper libraries)"
	@echo "  • Custom kebab-case conversion for Clojure idioms"
	@echo "  • Protobuf 4.29.5 (bundled)"
	./scripts/compile-protos.sh
	@echo "Applying protobuf compatibility fixes..."
	./scripts/fix-proto-compatibility.sh

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
test: ## Run tests
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

# Format code
.PHONY: fmt
fmt: ## Format all Clojure code using cljfmt
	@echo "Formatting Clojure code..."
	clojure -M:format fix
	@echo "✓ Code formatting complete"

# Check formatting
.PHONY: fmt-check
fmt-check: ## Check if code is properly formatted (exit 1 if not)
	@echo "Checking code formatting..."
	@if clojure -M:format check 2>&1 | grep -q "files formatted incorrectly"; then \
		echo "❌ Code formatting issues found!"; \
		echo "Run 'make fmt' to fix them."; \
		clojure -M:format check; \
		exit 1; \
	else \
		echo "✓ All files are properly formatted"; \
	fi

# Lint Clojure code with clj-kondo
.PHONY: lint-clj
lint-clj: ## Clojure linting with clj-kondo (v2025.06.05) - configured for Guardrails, Seesaw, Telemere
	@echo "Linting Clojure code with clj-kondo..."
	@echo "  • Guardrails support: >defn, >defn-, >def properly recognized"
	@echo "  • Seesaw macros: UI construction functions linted correctly"
	@echo "  • Telemere logging: Log macros understood without false positives"
	@clojure -M:lint --lint src test || exit 1
	@echo "✓ Clojure linting complete"

# Lint Kotlin code with ktlint
.PHONY: lint-kotlin
lint-kotlin: ## Kotlin style checking with ktlint (v1.5.0) - IntelliJ IDEA conventions
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
lint-kotlin-detekt: ## Advanced Kotlin analysis with detekt (v1.23.7) - complexity, code smells, performance
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

# Run all linters
.PHONY: lint
lint: fmt-check lint-clj lint-kotlin lint-kotlin-detekt ## Run all linters (clj-kondo, ktlint, detekt) - catches issues early
	@echo "✓ All linting checks passed!"

# Generate comprehensive lint report
.PHONY: lint-report
lint-report: ## Full report with all issues - includes errors and warnings from all linters
	@echo "Generating comprehensive lint report..."
	@echo "  • Summary of total errors and warnings"
	@echo "  • Breakdown by linter (clj-kondo, ktlint, detekt)"
	@echo "  • Issues organized by file with line numbers"
	@echo "  • Specific rule violations and descriptions"
	@bb scripts/lint-report.bb
	@echo "✓ Report generated at reports/lint-report.md"

# Generate filtered lint report
.PHONY: lint-report-filtered
lint-report-filtered: ## Report with false positive filtering (recommended) - filters ~56% of false positives
	@echo "Generating filtered lint report..."
	@echo "  • Filters Seesaw UI patterns (:text, :items, :border)"
	@echo "  • Filters Telemere functions (handler:console)"
	@echo "  • Filters Guardrails symbols (>, |, ?, =>)"
	@echo "  • Filters standard library false warnings"
	@bb scripts/lint-report-filtered.bb
	@echo "✓ Filtered report generated at reports/lint-report-filtered.md"

# Generate warnings-only lint report
.PHONY: lint-report-warnings
lint-report-warnings: ## Report excluding errors - focus on warnings only
	@echo "Generating warnings-only lint report..."
	@bb scripts/lint-report.bb --no-errors
	@echo "✓ Warnings report generated at reports/lint-report.md"

.PHONY: all
all: clean build ## Clean and build everything
