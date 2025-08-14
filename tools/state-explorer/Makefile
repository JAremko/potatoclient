# State-Explorer Makefile
# Tool for capturing and analyzing WebSocket state messages from /ws/ws_state endpoint
#
# Default behavior: Show help message
# Common usage: make run (captures 1 state payload)

.PHONY: help build clean-build run capture capture-many clean test repl deps view-latest view-compact proto analyze

# Default target - show help when user just types 'make'
.DEFAULT_GOAL := help

# Help target - displays all available commands
help:
	@echo "╔════════════════════════════════════════════════════════════════╗"
	@echo "║    State-Explorer - WebSocket State Endpoint Analysis Tool     ║"
	@echo "╚════════════════════════════════════════════════════════════════╝"
	@echo ""
	@echo "QUICK START:"
	@echo "  make            # Show this help"
	@echo "  make run        # Capture 1 state payload (most common usage)"
	@echo "  make view-latest # View the captured data"
	@echo ""
	@echo "CAPTURE COMMANDS:"
	@echo "  run           - Capture 1 state payload from sych.local"
	@echo "  capture       - Capture with custom parameters (see examples)"
	@echo "  capture-many  - Capture 10 payloads for analysis"
	@echo "  watch         - Continuous capture mode (every 5 seconds)"
	@echo ""
	@echo "ANALYSIS COMMANDS:"
	@echo "  view-latest   - Pretty-print the most recent EDN capture (zprint)"
	@echo "  view-compact  - Compact view of latest capture (80 char width)"
	@echo "  analyze       - Analyze all captured EDN files"
	@echo ""
	@echo "BUILD & MAINTENANCE:"
	@echo "  build         - Build protobuf classes (automatic if needed)"
	@echo "  clean         - Remove captured files only"
	@echo "  clean-build   - Remove all artifacts and captures"
	@echo "  deps          - Download dependencies"
	@echo "  deps-outdated - Check for outdated dependencies"
	@echo "  deps-upgrade  - Interactively upgrade dependencies"
	@echo ""
	@echo "DEVELOPMENT:"
	@echo "  repl          - Start Clojure REPL"
	@echo "  test          - Run test suite"
	@echo ""
	@echo "EXAMPLES:"
	@echo "  make capture DOMAIN=prod.example.com COUNT=5"
	@echo "  make capture COUNT=100 OUTPUT=/tmp/analysis"
	@echo "  make capture DOMAIN=localhost NO_SSL=true VERBOSE=true"
	@echo ""
	@echo "OUTPUT:"
	@echo "  Files are saved in ./output/ as:"
	@echo "  - <timestamp>.bin  (raw protobuf binary)"
	@echo "  - <timestamp>.edn  (human-readable Clojure data)"
	@echo ""

# === BUILD TARGETS ===

# Generate proto Java sources using protogen Docker
# Build is now just compiling Pronto (proto classes come from shared)
build:
	@echo "Setting up classpath with shared proto classes..."
	@echo "Build complete!"

# Remove all build artifacts and captured files
# Use this to start fresh
clean-build:
	@echo "Cleaning build artifacts..."
	@clojure -T:build clean
	@rm -rf output/*.bin output/*.edn

# === CAPTURE TARGETS ===

# Default capture - grabs 1 state payload from sych.local
# This is the most common usage for quick testing
run: ensure-proto
	@echo "Capturing 1 state payload from sych.local..."
	@clojure -M:run

# Parameterized capture - allows customization
# Examples:
#   make capture DOMAIN=myserver.com COUNT=5
#   make capture COUNT=20 OUTPUT=/tmp/captures
capture: ensure-proto
	@echo "Starting capture..."
	@clojure -M:run \
		$(if $(DOMAIN),-d $(DOMAIN)) \
		$(if $(COUNT),-c $(COUNT)) \
		$(if $(OUTPUT),-o $(OUTPUT)) \
		$(if $(NO_SSL),--no-ssl) \
		$(if $(VERBOSE),-v)

# Capture multiple payloads (10 by default)
# Useful for analyzing state changes over time
capture-many: ensure-proto
	@echo "Capturing 10 state payloads..."
	@clojure -M:run -c 10

# === MAINTENANCE TARGETS ===

# Clean only captured output files, preserves build
clean:
	@echo "Cleaning output directory..."
	@rm -rf output/*
	@echo "Cleaned."

# Run test suite
test:
	@echo "Running tests..."
	@clojure -M:test

# Start REPL for interactive development
repl:
	@echo "Starting REPL..."
	@clojure -M:repl

# Download all required dependencies
deps:
	@echo "Downloading dependencies..."
	@clojure -P -M:run
	@echo "Dependencies ready."

# === INTERNAL TARGETS ===

# Ensure protobuf classes are available before running
# Automatically builds if needed
ensure-proto:
	@if [ ! -d "target/classes/ser" ]; then \
		echo "Protobuf classes not found. Building..."; \
		$(MAKE) build; \
	fi

# === ANALYSIS TARGETS ===

# View the most recent EDN capture in pretty-printed format using zprint
view-latest:
	@if [ -d "output" ] && [ "$$(ls -A output/*.edn 2>/dev/null)" ]; then \
		latest=$$(ls -t output/*.edn | head -n1); \
		echo "Viewing $$latest:"; \
		echo ""; \
		cat $$latest | clojure -M -e "(require '[zprint.core :as zp]) (-> *in* slurp read-string (zp/zprint {:width 120}))"; \
	else \
		echo "No EDN files found in output directory"; \
	fi

# View EDN file with compact formatting
view-compact:
	@if [ -d "output" ] && [ "$$(ls -A output/*.edn 2>/dev/null)" ]; then \
		latest=$$(ls -t output/*.edn | head -n1); \
		echo "Compact view of $$latest:"; \
		echo ""; \
		cat $$latest | clojure -M -e "(require '[zprint.core :as zp]) (-> *in* slurp read-string (zp/zprint {:width 80 :map {:comma? false} :style :community}))"; \
	else \
		echo "No EDN files found in output directory"; \
	fi

# === DEVELOPMENT TARGETS ===

# Continuous capture mode - captures every 5 seconds
# Useful for monitoring state changes in real-time
watch:
	@echo "Starting continuous capture mode (Ctrl+C to stop)..."
	@while true; do \
		make run; \
		echo ""; \
		echo "Waiting 5 seconds before next capture..."; \
		sleep 5; \
	done

# Analyze all captured EDN files
# Shows file sizes and top-level keys for each capture
analyze:
	@if [ -d "output" ] && [ "$$(ls -A output/*.edn 2>/dev/null)" ]; then \
		echo "Analyzing captured EDN files..."; \
		for file in output/*.edn; do \
			echo ""; \
			echo "File: $$file"; \
			echo "Size: $$(wc -c < $$file) bytes"; \
			echo "Top-level keys:"; \
			cat $$file | clojure -M -e "(-> *in* slurp read-string keys println)"; \
		done; \
	else \
		echo "No EDN files found to analyze"; \
	fi

# Check for outdated dependencies
deps-outdated:
	@echo "Checking for outdated dependencies..."
	@clojure -Sdeps '{:aliases {:outdated {:extra-deps {com.github.liquidz/antq {:mvn/version "2.7.1133"}} :main-opts ["-m" "antq.core"]}}}' -M:outdated

# Interactively upgrade dependencies
deps-upgrade:
	@echo "Interactive dependency upgrade..."
	@clojure -Sdeps '{:aliases {:outdated {:extra-deps {com.github.liquidz/antq {:mvn/version "2.7.1133"}} :main-opts ["-m" "antq.core" "--upgrade"]}}}' -M:outdated
