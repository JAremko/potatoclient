#!/bin/bash

# Docstring Checker - Find definitions without documentation
# Usage: ./check.sh [directory]
# If no directory specified, defaults to ../../src

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$SCRIPT_DIR/../.."
DEFAULT_PATH="$PROJECT_ROOT/src"

# Use provided path or default to src
TARGET_PATH="${1:-$DEFAULT_PATH}"

# Make path absolute if relative
if [[ ! "$TARGET_PATH" = /* ]]; then
    TARGET_PATH="$PWD/$TARGET_PATH"
fi

echo "Docstring Checker"
echo "================="
echo ""

cd "$SCRIPT_DIR"
clojure -M:run "$TARGET_PATH"