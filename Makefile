# PotatoClient - Self-Documenting Build System
# 
# This Makefile provides all build, development, and quality assurance commands
# with comprehensive inline documentation. Run 'make help' for a complete overview.
#
# For detailed project documentation, see:
# - CLAUDE.md - Main developer guide
# - docs/ - Comprehensive documentation directory
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
	@grep -E '^(build|release|compile-kotlin|clean|rebuild):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
	@echo ""
	@echo "CODE QUALITY:"
	@grep -E '^(fmt|lint|lint-raw):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
	@echo ""
	@echo "DEPENDENCY MANAGEMENT:"
	@grep -E '^(deps-outdated|deps-upgrade|deps-upgrade-all|check-deps):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
	@echo ""
	@echo "OTHER TOOLS:"
	@grep -E '^(test|report-unspecced|build-macos-dev):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
	@echo ""
	@echo "MCP SERVER (Claude integration):"
	@grep -E '^(mcp-server|mcp-configure):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'
	@echo ""
	@echo "DOCUMENTATION:"
	@echo "  - Project guide: CLAUDE.md"
	@echo "  - Architecture docs: docs/architecture/"
	@echo "  - Development guides: docs/development/"
	@echo "  - Tool documentation: docs/tools/"
	@echo "  - Custom lint reports: bb scripts/lint-report.bb --help"
	@echo ""
	@echo "For detailed documentation, see CLAUDE.md"
	@echo "For custom lint report options, see: bb scripts/lint-report.bb --help"

###############################################################################
# BUILD TARGETS
###############################################################################

# Proto generation has been moved to shared module
# Run 'make proto' in the shared/ directory to regenerate proto classes


# Compile pronto Java classes if needed
.PHONY: ensure-pronto
ensure-pronto: ## Ensure pronto Java classes are compiled
	@if [ ! -d "examples/pronto/target/classes/pronto" ]; then \
		echo "Compiling pronto Java classes..."; \
		cd examples/pronto && \
		mkdir -p target/classes && \
		javac -cp "$$(clojure -Spath)" -d target/classes src/java/pronto/*.java; \
		echo "Pronto compilation successful"; \
	else \
		echo "Pronto classes already compiled"; \
	fi


# Ensure shared module proto classes are compiled
.PHONY: ensure-shared-compiled
ensure-shared-compiled: ## Ensure shared module proto/java classes are compiled
	@if [ ! -d "shared/target/classes/cmd" ] || [ ! -d "shared/target/classes/potatoclient/java/ipc" ]; then \
		echo "Shared module classes not found. Compiling shared module..."; \
		cd shared && $(MAKE) proto && cd ..; \
	else \
		echo "Shared module classes already compiled"; \
	fi

# Compile Kotlin sources
.PHONY: compile-kotlin
compile-kotlin: ensure-shared-compiled ## Compile Kotlin source files
	@echo "Compiling Kotlin sources..."
	clojure -T:build compile-kotlin


# Build target
.PHONY: build
build: ensure-compiled ## Build the project (creates JAR file)
	@echo "Building project..."
	clojure -T:build uber

# Build with forced clean
.PHONY: build-clean
build-clean: compile-kotlin ## Build with forced clean rebuild
	@echo "Building project with clean rebuild..."
	clojure -T:build uber

# Release build target
.PHONY: release
release: ## Build optimized JAR for distribution (stripped metadata, WARN/ERROR logging only, AOT compiled)
	@echo "Building release version..."
	@echo "  ✓ Only WARN/ERROR logging to platform-specific locations"
	@echo "  ✓ Window title shows [RELEASE]"
	@echo "  ✓ Metadata stripped (smaller JAR)"
	@echo "  ✓ AOT compilation and direct linking"
	@echo "  ✓ Release JARs auto-detect they're release builds"
	@echo ""
	POTATOCLIENT_RELEASE=true clojure -T:build release


# Clean target
.PHONY: clean
clean: clean-cache ## Clean all build artifacts
	@echo "Cleaning build artifacts..."
	clojure -T:build clean
	rm -rf target/
	rm -rf dist/
	# Also clean shared module completely to ensure fresh rebuild
	cd shared && $(MAKE) clean-all || true

# NREPL target
.PHONY: nrepl
nrepl: compile-kotlin ## REPL on port 7888 for interactive development (same validation features as make dev)
	@echo "Starting NREPL server on port 7888..."
	@echo "  ✓ EDN state validation enabled"
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
test: ensure-compiled ## Run tests (saves output to logs/test-runs/TIMESTAMP/)
	@echo "Setting up test logging..."
	@TEST_RUN_DIR=$$(./scripts/setup-test-logs.sh) && \
	echo "Test output will be saved to: $$TEST_RUN_DIR" && \
	echo "Compiling test Java files..." && \
	mkdir -p target/test-classes && \
	javac -cp "$$(clojure -M:test -Spath):target/classes" -d target/test-classes test/java/potatoclient/test/*.java 2>/dev/null || true && \
	echo "Running Clojure tests..." && \
	clojure -M:test 2>&1 | tee "$$TEST_RUN_DIR/test-full.log"; \
	EXIT_CODE=$$?; \
	echo "" && \
	echo "Generating test summary..." && \
	./scripts/compact-test-logs.sh "$$TEST_RUN_DIR/test-full.log" && \
	cd logs/test-runs && ln -sf "$$(basename $$TEST_RUN_DIR)" latest && cd - >/dev/null && \
	exit $$EXIT_CODE

# Kotlin test compilation (removed from main test suite)
# .PHONY: compile-kotlin-tests
# compile-kotlin-tests: ## Compile Kotlin test files
# 	@echo "Compiling Kotlin test files..."
# 	clojure -T:build compile-kotlin-tests

# Run Java tests
.PHONY: java-test
java-test: ## Compile and run Java tests (IPC tests)
	@echo "Running Java IPC tests..."
	clojure -T:build run-java-tests

# Kotlin tests (removed from main test suite)
# .PHONY: kotlin-test
# kotlin-test: ## Compile and run Kotlin tests (IPC and Transit tests)
# 	@echo "Running Kotlin tests..."
# 	clojure -T:build run-kotlin-tests

# View latest test results
.PHONY: test-summary
test-summary: ## View summary of the latest test run
	@if [ -L "logs/test-runs/latest" ] && [ -f "logs/test-runs/latest/test-full-summary.txt" ]; then \
		cat "logs/test-runs/latest/test-full-summary.txt"; \
		echo ""; \
		echo "Full log: logs/test-runs/latest/test-full.log"; \
		echo "Failures: logs/test-runs/latest/test-full-failures.txt"; \
	else \
		echo "No test results found. Run 'make test' first."; \
	fi

# Test with code coverage
.PHONY: test-coverage
test-coverage: build ## Run tests with comprehensive code coverage analysis for Clojure, Java, and Kotlin
	@echo "Running tests with code coverage..."
	@echo "  • Clojure coverage via Cloverage"
	@echo "  • Java/Kotlin coverage via JaCoCo"
	@echo "  • Generates HTML reports and lists uncovered functions"
	@echo ""
	./scripts/generate-coverage-report.sh

# Quick coverage for Clojure only
.PHONY: coverage-clojure
coverage-clojure: build ## Run Clojure tests with Cloverage (faster than full coverage)
	@echo "Running Clojure tests with coverage..."
	@echo "Note: This will show coverage for Clojure code only (not Java/Kotlin)"
	./scripts/run-clojure-coverage.sh

# Coverage report analysis
.PHONY: coverage-analyze
coverage-analyze: ## Analyze existing coverage reports and list uncovered functions
	@echo "Analyzing coverage reports..."
	./scripts/analyze-uncovered-code.sh
	@echo ""
	@echo "Uncovered functions report: target/coverage-reports/uncovered-functions.txt"

# Report unspecced functions
.PHONY: report-unspecced
report-unspecced: ## Check which functions need Malli specs - mandatory for all functions
	@echo "Generating unspecced functions report..."
	@echo "  • Lists all functions that lack Malli instrumentation"
	@echo "  • Groups them by namespace"
	@echo "  • Provides statistics on coverage"
	@echo "  • Remember: Add :malli/schema metadata to ALL functions"
	@echo ""
	@echo "Note: This report is no longer applicable - Guardrails has been removed"
	@echo "All functions should now use Malli :malli/schema metadata instead"
	@echo ""
	@echo "Report saved to ./reports/unspecced-functions.md"

# Generate clj-kondo type configs from Malli specs
.PHONY: kondo-configs
kondo-configs: ensure-compiled ## Generate clj-kondo type configs from function specs (includes shared module)
	@echo "Generating clj-kondo type configs for entire project..."
	@echo "  • Collecting Malli function schemas"
	@echo "  • Including shared module definitions"
	@echo "  • Extracting type information from Malli specs"
	@echo "  • Writing to .clj-kondo/metosin/malli-types-clj/config.edn"
	@echo ""
	@# Ensure shared module is compiled
	@if [ ! -d "shared/target/classes/cmd" ]; then \
		echo "Shared module proto classes not found. Compiling first..."; \
		cd shared && $(MAKE) compile && cd ..; \
	fi
	@clojure -M:dev scripts/generate-kondo-configs.clj root
	@echo ""
	@echo "✓ Type configs generated for entire project"
	@echo "  Location: .clj-kondo/metosin/malli-types-clj/config.edn"
	@echo "  Includes: Both root and shared module definitions"

# Generate clj-kondo configs for shared module only
.PHONY: kondo-configs-shared
kondo-configs-shared: ## Generate clj-kondo type configs for shared module only
	@echo "Generating clj-kondo type configs for shared module..."
	@cd shared && $(MAKE) kondo-configs

# Validate Action Registry
.PHONY: validate-actions
validate-actions: ## Validate Action Registry against protobuf structure
	@echo "Validating Action Registry..."
	@echo "  • Compares registered actions with proto commands"
	@echo "  • Identifies missing or extra actions"
	@echo "  • Checks parameter consistency"
	@echo ""
	@cd tools/action-validator && $(MAKE) validate-dev

# Simple coverage target (alias for coverage-clojure)
.PHONY: coverage
coverage: coverage-clojure ## Generate code coverage report (alias for coverage-clojure)

# Check dependencies
.PHONY: check-deps
check-deps: ## Check that required tools are installed
	@echo "Checking dependencies..."
	@command -v clojure >/dev/null 2>&1 || { echo "Error: clojure not found. Please install Clojure CLI."; exit 1; }
	@command -v java >/dev/null 2>&1 || { echo "Error: java not found. Please install Java."; exit 1; }
	@echo "All dependencies found!"

# Check for outdated dependencies
.PHONY: deps-outdated
deps-outdated: ## Check for outdated dependencies using antq
	@echo "Checking for outdated dependencies..."
	@echo "  • Scans deps.edn for newer versions"
	@echo "  • Shows available updates in a table"
	@echo "  • Includes both direct and transitive dependencies"
	@echo ""
	@clojure -M:outdated

# Interactively upgrade dependencies
.PHONY: deps-upgrade
deps-upgrade: ## Interactively upgrade outdated dependencies
	@echo "Interactive dependency upgrade..."
	@echo "  • Shows each outdated dependency"
	@echo "  • Lets you choose which to upgrade"
	@echo "  • Automatically updates deps.edn"
	@echo "  • Downloads new dependencies"
	@echo ""
	@clojure -M:outdated-upgrade

# Force upgrade all dependencies
.PHONY: deps-upgrade-all
deps-upgrade-all: ## Upgrade all outdated dependencies (non-interactive)
	@echo "Upgrading all outdated dependencies..."
	@echo "  ⚠️  This will automatically update ALL outdated dependencies"
	@echo "  • Updates deps.edn without confirmation"
	@echo "  • Downloads all new versions"
	@echo "  • Remember to test after upgrading!"
	@echo ""
	@read -p "Are you sure? (y/N) " -n 1 -r; \
	echo ""; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		clojure -M:outdated --upgrade --force; \
		echo "✓ Dependencies upgraded. Remember to run 'make test' to verify!"; \
	else \
		echo "Upgrade cancelled."; \
	fi

# Quick rebuild
.PHONY: rebuild
rebuild: clean build ## Clean and rebuild the project

###############################################################################
# PRIMARY DEVELOPMENT COMMANDS
###############################################################################

# Ensure everything is compiled without unnecessary cleaning
.PHONY: ensure-compiled
ensure-compiled: clean-app ## Compile only if sources changed (smart compilation)
	@echo "Recompiling Kotlin classes to ensure fresh code..."
	@$(MAKE) compile-kotlin
	@echo "All required files are compiled."

# Development target - runs with all validation, logging, and debugging features
.PHONY: dev
dev: ensure-compiled ## PRIMARY DEVELOPMENT COMMAND - Full validation, all logs, warnings (takes 30-40s to start)
	@echo "Running development version with:"
	@echo "  ✓ Full Malli validation catches bugs immediately"
	@echo "  ✓ EDN state validation enabled (protobuf constraint checking)"
	@echo "  ✓ Reflection warnings for performance issues"
	@echo "  ✓ All log levels (DEBUG, INFO, WARN, ERROR) to console and ./logs/"
	@echo "  ✓ GStreamer debug output (GST_DEBUG=3)"
	@echo "  ✓ Window title shows [DEVELOPMENT]"
	@echo "  ✓ Running from source (no AOT compilation)"
	@echo ""
	@echo "⚠️  Takes 30-40 seconds to start - set appropriate timeouts in your tools!"
	@echo ""
	GST_DEBUG=3 clojure -M:run

# Development with clean rebuild
.PHONY: dev-clean
dev-clean: clean-cache compile-kotlin ## Development with forced clean rebuild (regenerates everything)
	@echo "Running development version with clean rebuild..."
	@$(MAKE) dev

# Run the JAR file in production-like mode
.PHONY: run
run: build ## Test the JAR in production-like mode (near-production speed)
	@echo "Running JAR in production-like mode..."
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

# Clean only Clojure classes (preserves proto and kotlin)
.PHONY: clean-clojure
clean-clojure: ## Clean only Clojure compiled classes
	@echo "Cleaning Clojure classes..."
	@rm -rf .cpcache/
	@rm -rf target/classes/potatoclient/
	@find src/potatoclient -name "*.class" -type f -delete 2>/dev/null || true

# Clean Clojure and Kotlin classes (preserves proto)
.PHONY: clean-app
clean-app: ## Clean Clojure and Kotlin compiled classes (preserves proto)
	@echo "Cleaning Clojure and Kotlin classes..."
	@rm -rf .cpcache/
	@rm -rf target/classes/potatoclient/
	@rm -rf target/classes/i18n/
	@find src/potatoclient -name "*.class" -type f -delete 2>/dev/null || true

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
