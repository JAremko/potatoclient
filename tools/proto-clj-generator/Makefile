# Proto-CLJ Generator Makefile
# This Makefile helps compile protobuf classes and run tests

.PHONY: help
help: ## Show this help
	@echo "Proto-CLJ Generator Build Commands:"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'

.PHONY: generate-proto-java
generate-proto-java: ## Generate Java protobuf sources from main project
	@echo "Generating Java protobuf sources..."
	@clojure -T:build generate-proto-java

.PHONY: compile-proto
compile-proto: ## Compile protobuf Java classes locally
	@echo "Compiling Java protobuf classes..."
	@clojure -T:build compile-java-proto

.PHONY: generate
generate: ## Generate Clojure converters with namespace separation (default)
	@echo "Generating Clojure protobuf converters (namespace separation)..."
	@clojure -X:gen

.PHONY: generate-single
generate-single: ## Generate Clojure converters in single file mode (legacy)
	@echo "Generating Clojure protobuf converters (single file mode)..."
	@clojure -X:gen-single

.PHONY: lint-generated
lint-generated: generate ## Lint the generated code
	@echo "Linting generated code..."
	@cd ../.. && clj-kondo --lint shared/potatoclient/proto

.PHONY: generate-test-code
generate-test-code: compile-proto ## Generate code for tests
	@echo "Generating test code..."
	@rm -rf test-roundtrip-output
	@clojure -M:gen generate-all \
		--input-dir "../proto-explorer/output/json-descriptors" \
		--output-dir "test-roundtrip-output" \
		--namespace-prefix "test.roundtrip" \
		--guardrails

.PHONY: test
test: generate-test-code ## Run all tests (generates test code first)
	@echo "Running tests..."
	@clojure -X:test

.PHONY: test-availability
test-availability: ## Test if proto classes are available
	@echo "Testing proto class availability..."
	@clojure -X:test :nses '[generator.proto-availability-test]'

.PHONY: test-roundtrip
test-roundtrip: ## Run comprehensive roundtrip tests
	@echo "Running roundtrip tests..."
	@clojure -X:test :nses '[generator.comprehensive-roundtrip-test]'

.PHONY: test-watch
test-watch: ## Run tests in watch mode
	@echo "Running tests in watch mode..."
	@clojure -X:test :watch? true

.PHONY: repl
repl: ## Start a REPL with test dependencies
	@echo "Starting REPL..."
	@clojure -M:dev:test

.PHONY: guardrails-check
guardrails-check: ## Check guardrails coverage for generated code
	@echo "Checking guardrails coverage..."
	@clojure -X:guardrails-check :namespaces '[potatoclient.proto.cmd potatoclient.proto.ser]'

.PHONY: nrepl
nrepl: ## Start nREPL server on port 7888
	@echo "Starting nREPL on port 7888..."
	@clojure -M:dev:test:nrepl

.PHONY: clean
clean: ## Clean generated files
	@echo "Cleaning..."
	@rm -rf proto-classes/
	@rm -rf generated/
	@rm -rf /home/jare/git/potatoclient/shared/potatoclient/proto/

.PHONY: all
all: clean compile-proto generate test ## Build everything and run tests