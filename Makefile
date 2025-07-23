# PotatoClient - Local Development Makefile
# Default target shows help
.DEFAULT_GOAL := help

# Variables
# Version is read from VERSION file
JAR_VERSION = $(shell cat VERSION)
JAR_NAME = potatoclient-$(JAR_VERSION).jar
JAR_PATH = target/$(JAR_NAME)

# Help target
.PHONY: help
help: ## Show this help message
	@echo "PotatoClient - Local Development"
	@echo ""
	@echo "Usage: make [target]"
	@echo ""
	@echo "Build & Run:"
	@grep -E '^(build|release|dev|clean|rebuild|proto|compile-kotlin|test):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-15s %s\n", $$1, $$2}'
	@echo ""
	@echo "MCP Server (for Claude integration):"
	@grep -E '^(mcp-serve|mcp-configure):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-15s %s\n", $$1, $$2}'
	@echo ""
	@echo "Development:"
	@grep -E '^(nrepl|check-deps|build-macos-dev|report-unspecced|fmt|fmt-check):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-15s %s\n", $$1, $$2}'
	@echo ""
	@echo "Quick Start with MCP:"
	@echo "  1. make mcp-server    # Start MCP server (keep terminal open)"
	@echo "  2. In Claude, use /mcp command to connect"

# Build proto files
.PHONY: proto
proto: ## Generate Java classes from proto files
	@echo "Generating Java classes from proto files..."
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
release: ## Build release version (without instrumentation)
	@echo "Building release version..."
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
nrepl: proto compile-kotlin compile-java-proto ## Start NREPL server on port 7888 for development
	@echo "Starting NREPL server on port 7888..."
	@echo "Connect your editor to port 7888"
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
report-unspecced: proto compile-kotlin compile-java-proto ## Generate report of functions without Malli specs
	@echo "Generating unspecced functions report..."
	@echo "Running report from source..."
	clojure -M:run --report-unspecced

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

# Development target - runs with all validation, logging, and debugging features
.PHONY: dev
dev: clean-cache proto compile-kotlin compile-java-proto ## Run development version with full validation, reflection warnings, and debug logging
	@echo "Running development version with:"
	@echo "  ✓ Full Guardrails validation"
	@echo "  ✓ Reflection warnings"
	@echo "  ✓ All log levels (DEBUG, INFO, WARN, ERROR)"
	@echo "  ✓ GStreamer debug output"
	@echo "  ✓ Running from source (no AOT compilation)"
	@echo ""
	GST_DEBUG=3 clojure -M:run

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

.PHONY: all
all: clean build ## Clean and build everything
