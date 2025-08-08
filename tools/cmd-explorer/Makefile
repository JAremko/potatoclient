# CMD-Explorer Makefile
# Tool for testing and exploring WebSocket command endpoints

.PHONY: help deps extract extract-validate extract-dry-run test repl clean

# Default target
.DEFAULT_GOAL := help

help:
	@echo "╔════════════════════════════════════════════════════════════════╗"
	@echo "║          CMD-Explorer - WebSocket Command Testing Tool          ║"
	@echo "╚════════════════════════════════════════════════════════════════╝"
	@echo ""
	@echo "SETUP:"
	@echo "  deps              - Download dependencies"
	@echo "  extract           - Extract TypeScript functions and update TODO"
	@echo "  extract-validate  - Validate extraction without updating"
	@echo "  extract-dry-run   - Show what would be extracted"
	@echo ""
	@echo "DEVELOPMENT:"
	@echo "  repl              - Start Clojure REPL"
	@echo "  test              - Run test suite"
	@echo ""
	@echo "MAINTENANCE:"
	@echo "  clean             - Remove generated files"
	@echo "  backup-list       - List TODO backups"
	@echo "  deps-outdated     - Check for outdated dependencies"
	@echo "  deps-upgrade      - Interactively upgrade dependencies"
	@echo ""

# Download dependencies
deps:
	@echo "Downloading dependencies..."
	@clojure -P -M:extract
	@echo "Dependencies ready."

# Extract TypeScript functions and update TODO
extract:
	@echo "Extracting TypeScript functions..."
	@clojure -M:extract

# Validate extraction without updating
extract-validate:
	@echo "Validating extraction..."
	@clojure -M:extract --validate

# Dry run - show what would be extracted
extract-dry-run:
	@echo "Dry run extraction..."
	@clojure -M:extract --dry-run

# Run tests
test:
	@echo "Running tests..."
	@clojure -M:test

# Start REPL
repl:
	@echo "Starting REPL..."
	@clojure -M:repl

# Clean generated files
clean:
	@echo "Cleaning..."
	@rm -rf target .cpcache

# List TODO backups
backup-list:
	@echo "TODO backups:"
	@ls -la bkp/todo_*.md 2>/dev/null || echo "No backups found"

# Restore specific backup (usage: make restore-backup FILE=bkp/todo_20240108_120000.md)
restore-backup:
	@if [ -z "$(FILE)" ]; then \
		echo "Usage: make restore-backup FILE=bkp/todo_TIMESTAMP.md"; \
		exit 1; \
	fi
	@if [ -f "$(FILE)" ]; then \
		cp todo.md bkp/todo_before_restore_$$(date +%Y%m%d_%H%M%S).md; \
		cp $(FILE) todo.md; \
		echo "Restored $(FILE) to todo.md"; \
	else \
		echo "File $(FILE) not found"; \
		exit 1; \
	fi

# Check for outdated dependencies
deps-outdated:
	@echo "Checking for outdated dependencies..."
	@clojure -Sdeps '{:aliases {:outdated {:extra-deps {com.github.liquidz/antq {:mvn/version "2.7.1133"}} :main-opts ["-m" "antq.core"]}}}' -M:outdated

# Interactively upgrade dependencies
deps-upgrade:
	@echo "Interactive dependency upgrade..."
	@clojure -Sdeps '{:aliases {:outdated {:extra-deps {com.github.liquidz/antq {:mvn/version "2.7.1133"}} :main-opts ["-m" "antq.core" "--upgrade"]}}}' -M:outdated