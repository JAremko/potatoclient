#!/bin/bash

# Test runner for Clojure Stream Spawner

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "========================================="
echo "Running Clojure Stream Spawner Tests"
echo "========================================="
echo ""

# Ensure Java classes are available for tests
if [ ! -d "$PROJECT_ROOT/target/java-classes/potatoclient/java/ipc" ]; then
    echo "Compiling Java classes for tests..."
    cd "$PROJECT_ROOT"
    make compile-java
    cd "$SCRIPT_DIR"
fi

# Run tests
echo "Running tests..."
clj -M:test "$@"