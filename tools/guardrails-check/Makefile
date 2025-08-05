.PHONY: help check report stats list find test clean

# Default source directory
SRC_DIR ?= ../../src/potatoclient

help: ## Show this help
	@echo "Guardrails Check Tool"
	@echo
	@echo "Usage: make [target] [SRC_DIR=path]"
	@echo
	@echo "Targets:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-15s %s\n", $$1, $$2}'
	@echo
	@echo "Examples:"
	@echo "  make check                  # Check default src/potatoclient"
	@echo "  make report SRC_DIR=../src  # Generate report for custom directory"
	@echo "  make find PATTERN=process   # Find functions with 'process' in name"

check: ## Check for functions without Guardrails (EDN output)
	@bb check $(SRC_DIR)

report: ## Generate markdown report of functions without Guardrails
	@bb report $(SRC_DIR)

stats: ## Show statistics about Guardrails usage
	@bb stats $(SRC_DIR)

list: ## List namespaces with functions not using Guardrails
	@bb list $(SRC_DIR)

find: ## Find specific functions not using Guardrails (requires PATTERN)
	@if [ -z "$(PATTERN)" ]; then \
		echo "Error: PATTERN is required. Usage: make find PATTERN=<pattern>"; \
		exit 1; \
	fi
	@bb find "$(PATTERN)" $(SRC_DIR)

test: ## Run quick test on current project
	@echo "Running guardrails check on $(SRC_DIR)..."
	@bb stats $(SRC_DIR)

clean: ## Clean any temporary files
	@echo "No temporary files to clean"

# Save report to file
save-report: ## Save markdown report to file
	@mkdir -p reports
	@bb report $(SRC_DIR) > reports/guardrails-check-$$(date +%Y%m%d-%H%M%S).md
	@echo "Report saved to reports/"