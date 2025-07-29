#!/bin/bash
# Run Clojure coverage with proper classpath setup

set -e

echo "Running Clojure coverage with proper classpath..."

# Ensure we're in the project root
cd "$(dirname "$0")/.."

# Ensure classes are compiled first
if [ ! -d "target/classes" ] || [ -z "$(ls -A target/classes 2>/dev/null)" ]; then
    echo "Compiling classes first..."
    make build
fi

# Create coverage directory
mkdir -p target/coverage

# Run coverage - the test-coverage alias already includes target/classes
echo "Running cloverage..."
clojure -M:test-coverage \
    2>&1 | tee target/coverage/cloverage.log

echo ""
echo "Coverage report generated at: target/coverage/index.html"