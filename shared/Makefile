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

# Compile existing proto sources (no Docker, just javac)
compile: 
	@echo "Compiling existing proto sources..."
	clojure -T:build compile-all

# Run tests (requires proto classes to be compiled)
test: compile
	@echo "Running shared project tests..."
	clojure -M:test

# Full build with proto generation (only when protos change)
build-with-proto: proto compile
	@echo "Full build complete!"