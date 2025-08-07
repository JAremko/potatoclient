# Proto Tree Printer Tool
# Prints the full tree structure of protobuf messages

# Classpath for compiled proto classes
PROTO_CLASSPATH = generated-proto

.PHONY: help
help: ## Show this help
	@echo "Proto Tree Printer - Display protobuf message structures"
	@echo ""
	@echo "Usage:"
	@echo "  make print-cmd    - Print command message tree"
	@echo "  make print-state  - Print state message tree"
	@echo "  make print-both   - Print both trees"
	@echo ""
	@echo "Available targets:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-15s %s\n", $$1, $$2}'

.PHONY: print-cmd
print-cmd: ## Print command (cmd) root message tree
	@clojure -M -m proto-tree-printer.core cmd

.PHONY: print-state
print-state: ## Print state (ser) root message tree
	@clojure -M -m proto-tree-printer.core state

.PHONY: print-ser
print-ser: print-state ## Alias for print-state

.PHONY: print-both
print-both: ## Print both cmd and state message trees
	@clojure -M -m proto-tree-printer.core both

.PHONY: print-trees
print-trees: print-both ## Alias for print-both