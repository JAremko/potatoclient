#!/bin/bash

# Docstring Checker - Find definitions without documentation
# Usage: ./check.sh [directory]
# If no directory specified, scans both src/ and test/ from project root

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$SCRIPT_DIR/../.."

echo "Docstring Checker"
echo "================="
echo ""

# Change to project root so the tool finds the correct deps.edn
cd "$PROJECT_ROOT"

if [ $# -eq 0 ]; then
    # No arguments - run without args to scan both src and test
    (cd "$SCRIPT_DIR" && clojure -M:run)
else
    # Arguments provided - pass through
    (cd "$SCRIPT_DIR" && clojure -M:run "$@")
fi