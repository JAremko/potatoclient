# Shared Project Makefile
# Handles proto generation and compilation for the entire project

.PHONY: proto clean proto-sources proto-compile

# Generate and compile proto classes
proto: proto-sources proto-compile
	@echo "Proto generation and compilation complete!"

# Just generate proto sources
proto-sources:
	@echo "Generating proto Java sources..."
	./scripts/generate-protos.sh

# Compile proto sources to bytecode
proto-compile:
	@echo "Compiling proto Java sources to bytecode..."
	clojure -T:build compile-proto-sources

# Clean all generated artifacts
clean:
	@echo "Cleaning proto artifacts..."
	clojure -T:build clean
	@rm -rf src/java target

# Build all (proto + pronto + clojure)
build: proto
	@echo "Building shared project..."
	clojure -T:build compile-all

# Run tests
test: build
	@echo "Running shared project tests..."
	clojure -M:test