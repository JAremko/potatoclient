# PotatoClient - Local Development Makefile
# Default target shows help
.DEFAULT_GOAL := help

# Variables
JAR_NAME = potatoclient.jar
JAR_PATH = target/$(JAR_NAME)

# Help target
.PHONY: help
help: ## Show this help message
	@echo "PotatoClient - Local Development"
	@echo ""
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-15s %s\n", $$1, $$2}'
	@echo ""
	@echo "Examples:"
	@echo "  make build      # Build the project"
	@echo "  make run        # Build and run the application"
	@echo "  make nrepl      # Start NREPL for Clojure development"

# Build proto files
.PHONY: proto
proto: ## Generate Java classes from proto files
	@echo "Generating Java classes from proto files..."
	./scripts/compile-protos.sh

# Build target
.PHONY: build
build: proto ## Build the project (creates JAR file)
	@echo "Building project..."
	lein uberjar

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
	lein clean
	rm -rf target/
	rm -rf dist/

# NREPL target
.PHONY: nrepl
nrepl: ## Start NREPL server for development
	@echo "Starting NREPL server..."
	@echo "Connect your editor to the port shown below:"
	lein repl

# MCP target
.PHONY: mcp
mcp: ## Start Claude Code with MCP
	@echo "Starting Claude Code with MCP..."
	@echo "Make sure claude is in your PATH"
	claude code .

# Test target
.PHONY: test
test: ## Run tests
	@echo "Running tests..."
	lein test

# Check dependencies
.PHONY: check-deps
check-deps: ## Check that required tools are installed
	@echo "Checking dependencies..."
	@command -v lein >/dev/null 2>&1 || { echo "Error: lein not found. Please install Leiningen."; exit 1; }
	@command -v java >/dev/null 2>&1 || { echo "Error: java not found. Please install Java."; exit 1; }
	@echo "All dependencies found!"

# Quick rebuild
.PHONY: rebuild
rebuild: clean build ## Clean and rebuild the project

# Development run with debug
.PHONY: dev
dev: build ## Run with GStreamer debug output
	GST_DEBUG=3 java --enable-native-access=ALL-UNNAMED -jar $(JAR_PATH)

# List GStreamer plugins
.PHONY: list-gst-plugins
list-gst-plugins: ## List available GStreamer H264 plugins
	@echo "Listing GStreamer H264 plugins..."
	lein compile
	java -cp "$(shell lein classpath)" com.sycha.ListGStreamerPlugins

# Build for macOS development (unsigned, uses system Java)
.PHONY: build-macos-dev
build-macos-dev: ## Build unsigned macOS .app bundle for development
	@echo "Building macOS development version..."
	./scripts/build-macos-dev.sh

.PHONY: all
all: clean build ## Clean and build everything