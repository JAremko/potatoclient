# Kondo Config Generator
# Generate clj-kondo configs from Malli function schemas

.PHONY: help run test clean

help: ## Show this help
	@echo "Kondo Config Generator"
	@echo "====================="
	@echo ""
	@echo "Available targets:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-15s %s\n", $$1, $$2}'

run: ## Generate clj-kondo configs in project root
	@echo "Generating clj-kondo configs..."
	@cd ../.. && clojure -M:kondo-gen:run

test: ## Run tests
	@echo "Running tests..."
	@clojure -M:test

clean: ## Clean generated files
	@echo "Cleaning generated configs..."
	@rm -rf ../../.clj-kondo/config.edn
	@rm -rf ../../.clj-kondo/malli-schemas.edn
	@echo "Clean complete"

verify: ## Verify generated configs
	@echo "Verifying configs..."
	@if [ -f "../../.clj-kondo/config.edn" ]; then \
		echo "✓ Config file exists"; \
		clojure -e "(println \"✓ Config file is valid EDN:\" (count (read-string (slurp \"../../.clj-kondo/config.edn\"))) \"keys\")"; \
	else \
		echo "✗ Config file does not exist"; \
		exit 1; \
	fi

generate-and-verify: run verify ## Generate configs and verify them
	@echo "✓ Configs generated and verified successfully"