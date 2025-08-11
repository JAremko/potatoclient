# Validate Makefile
# Dual validation tool for protobuf binary payloads using both
# buf.validate constraints and Malli schemas in parallel
#
# Default behavior: Show help message
# Common usage: make validate FILE=path/to/file.bin
#
# === MALLI SPEC INTEGRATION ===
# This tool now includes comprehensive Malli specs for proto messages:
# - State specs: Hierarchical specs for JonSharedData$JonGUIState
# - Cmd specs: Hierarchical specs for JonSharedCmd$Root  
# - Property-based testing against buf.validate constraints
# - Generators that produce valid proto-compatible data
#
# === KEY RESOURCES ===
# Malli Documentation: https://github.com/metosin/malli
# - Generators: Use :gen/gen property for custom generators
# - Validation: malli.core/validate for spec validation
# - Property Testing: malli.generator/generate for test data
#
# Proto Explorer: Use proto-class-explorer agent to discover:
# - Proto message structures and field types
# - Buf.validate constraints per field
# - Java class to proto message mappings
#
# State Explorer Outputs: /tools/state-explorer/output/*.edn
# - Real captured state messages for reference
# - Use for validating spec accuracy

.PHONY: help build clean-build validate validate-cmd validate-state clean test repl deps proto compile ensure-proto spec-test generate-samples validate-specs property-test setup-symlinks

# Default target - show help when user just types 'make'
.DEFAULT_GOAL := help

# Help target - displays all available commands
help:
	@echo "╔════════════════════════════════════════════════════════════════╗"
	@echo "║      Validate - Dual Protobuf Binary Validation Tool           ║"
	@echo "║         (buf.validate + Malli validation in parallel)          ║"
	@echo "╚════════════════════════════════════════════════════════════════╝"
	@echo ""
	@echo "QUICK START:"
	@echo "  make            # Show this help"
	@echo "  make test       # Run tests"
	@echo "  make validate FILE=path/to/file.bin  # Validate a binary file"
	@echo ""
	@echo "VALIDATION COMMANDS:"
	@echo "  validate      - Validate binary file with dual validation (auto-detects type)"
	@echo "  validate-cmd  - Validate as command message with both validators"
	@echo "  validate-state - Validate as state message with both validators"
	@echo ""
	@echo "VALIDATION FEATURES:"
	@echo "  • Runs buf.validate and Malli validation in parallel"
	@echo "  • Handles empty, corrupted, and truncated files gracefully"
	@echo "  • Provides humanized error messages"
	@echo "  • Shows independent results from both validators"
	@echo ""
	@echo "BUILD & MAINTENANCE:"
	@echo "  build         - Build protobuf classes (automatic if needed)"
	@echo "  clean         - Remove temporary files"
	@echo "  clean-build   - Remove all artifacts"
	@echo "  deps          - Download dependencies"
	@echo "  deps-outdated - Check for outdated dependencies"
	@echo "  deps-upgrade  - Interactively upgrade dependencies"
	@echo ""
	@echo "DEVELOPMENT:"
	@echo "  repl          - Start Clojure REPL"
	@echo "  test          - Run test suite"
	@echo ""
	@echo "MALLI SPEC COMMANDS:"
	@echo "  setup-symlinks    - Set up symlinks to shared specs"
	@echo ""
	@echo "EXAMPLES:"
	@echo "  make validate FILE=output/state_20241208_143022.bin"
	@echo "  make validate-cmd FILE=commands.bin VERBOSE=true"
	@echo "  make validate-state FILE=state.bin OUTPUT=json"
	@echo ""

# === BUILD TARGETS ===

# Generate proto Java sources
proto:
	@echo "Generating proto Java sources..."
	@./scripts/generate-protos.sh

# Compile all Java sources
compile:
	@echo "Compiling all Java sources..."
	@clojure -T:build compile-all

# Build everything (generate + compile)
build: proto compile
	@echo "Build complete!"

# Remove all build artifacts
clean-build:
	@echo "Cleaning build artifacts..."
	@clojure -T:build clean
	@rm -rf target/

# === VALIDATION TARGETS ===

# Validate a binary file (auto-detects type)
validate: ensure-proto
	@if [ -z "$(FILE)" ]; then \
		echo "Error: Please specify FILE=path/to/file.bin"; \
		exit 1; \
	fi
	@echo "Validating $(FILE)..."
	@clojure -M:run -f "$(FILE)" \
		$(if $(TYPE),-t $(TYPE)) \
		$(if $(OUTPUT),-o $(OUTPUT)) \
		$(if $(VERBOSE),-v)

# Validate as command root message
validate-cmd: ensure-proto
	@if [ -z "$(FILE)" ]; then \
		echo "Error: Please specify FILE=path/to/file.bin"; \
		exit 1; \
	fi
	@echo "Validating $(FILE) as command message..."
	@clojure -M:run -f "$(FILE)" -t cmd \
		$(if $(OUTPUT),-o $(OUTPUT)) \
		$(if $(VERBOSE),-v)

# Validate as state root message
validate-state: ensure-proto
	@if [ -z "$(FILE)" ]; then \
		echo "Error: Please specify FILE=path/to/file.bin"; \
		exit 1; \
	fi
	@echo "Validating $(FILE) as state message..."
	@clojure -M:run -f "$(FILE)" -t state \
		$(if $(OUTPUT),-o $(OUTPUT)) \
		$(if $(VERBOSE),-v)

# === MAINTENANCE TARGETS ===

# Clean temporary files
clean:
	@echo "Cleaning temporary files..."
	@rm -rf .cpcache/
	@echo "Cleaned."

# Run test suite
test: ensure-proto
	@echo "Running tests..."
	@clojure -M:test

# Start REPL for interactive development
repl: ensure-proto
	@echo "Starting REPL..."
	@clojure -M:repl

# Download all required dependencies
deps:
	@echo "Downloading dependencies..."
	@clojure -P -M:run
	@echo "Dependencies ready."

# === INTERNAL TARGETS ===

# Ensure protobuf classes are available before running
ensure-proto:
	@if [ ! -d "target/classes/ser" ] || [ ! -d "target/classes/cmd" ]; then \
		echo "Protobuf classes not found. Building..."; \
		$(MAKE) build; \
	fi

# === DEVELOPMENT TARGETS ===

# Check for outdated dependencies
deps-outdated:
	@echo "Checking for outdated dependencies..."
	@clojure -Sdeps '{:aliases {:outdated {:extra-deps {com.github.liquidz/antq {:mvn/version "2.7.1133"}} :main-opts ["-m" "antq.core"]}}}' -M:outdated

# Interactively upgrade dependencies
deps-upgrade:
	@echo "Interactive dependency upgrade..."
	@clojure -Sdeps '{:aliases {:outdated {:extra-deps {com.github.liquidz/antq {:mvn/version "2.7.1133"}} :main-opts ["-m" "antq.core" "--upgrade"]}}}' -M:outdated

# === MALLI SPEC TARGETS ===

# Set up symlinks to shared specs
setup-symlinks:
	@echo "Setting up symlinks to shared specs..."
	@mkdir -p src/validate/specs
	@if [ ! -L "src/validate/specs/shared" ]; then \
		ln -s ../../../../shared/src/potatoclient/specs src/validate/specs/shared; \
		echo "Created symlink: src/validate/specs/shared -> shared/src/potatoclient/specs"; \
	else \
		echo "Symlink already exists: src/validate/specs/shared"; \
	fi
	@if [ ! -L "src/potatoclient" ]; then \
		ln -s ../../../shared/src/potatoclient src/potatoclient; \
		echo "Created symlink: src/potatoclient -> shared/src/potatoclient"; \
	else \
		echo "Symlink already exists: src/potatoclient"; \
	fi