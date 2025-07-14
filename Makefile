# PotatoClient - Local Development Makefile
# Default target shows help
.DEFAULT_GOAL := help

# Variables
# Version is defined in build.clj
JAR_VERSION = 1.2.4
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
	@grep -E '^(build|run|dev|clean|rebuild|proto|compile-java|test):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-15s %s\n", $$1, $$2}'
	@echo ""
	@echo "MCP Server (for Claude integration):"
	@grep -E '^(mcp-serve|mcp-configure):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-15s %s\n", $$1, $$2}'
	@echo ""
	@echo "Development:"
	@grep -E '^(nrepl|check-deps|build-macos-dev):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-15s %s\n", $$1, $$2}'
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

# Compile Java sources
.PHONY: compile-java
compile-java: ## Compile Java source files
	@echo "Compiling Java sources..."
	clojure -T:build compile-java

# Build target
.PHONY: build
build: proto compile-java ## Build the project (creates JAR file)
	@echo "Building project..."
	clojure -T:build uber

# Run target
.PHONY: run
run: build ## Build and run the application
	@echo "Running application..."
	java --enable-native-access=ALL-UNNAMED -jar $(JAR_PATH)

# Clean proto files
.PHONY: clean-proto
clean-proto: ## Clean generated proto Java files
	@echo "Cleaning generated proto files..."
	rm -rf src/java/ser/
	rm -rf src/java/cmd/

# Clean target
.PHONY: clean
clean: clean-proto ## Clean all build artifacts
	@echo "Cleaning build artifacts..."
	clojure -T:build clean
	rm -rf target/
	rm -rf dist/
	rm -rf .cpcache/

# NREPL target
.PHONY: nrepl
nrepl: proto compile-java ## Start NREPL server on port 7888 for development
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

# Development run with debug
.PHONY: dev
dev: build ## Run with GStreamer debug output
	GST_DEBUG=3 java --enable-native-access=ALL-UNNAMED -jar $(JAR_PATH)

# Build for macOS development (unsigned, uses system Java)
.PHONY: build-macos-dev
build-macos-dev: ## Build unsigned macOS .app bundle for development
	@echo "Building macOS development version..."
	./scripts/build-macos-dev.sh

.PHONY: all
all: clean build ## Clean and build everything
