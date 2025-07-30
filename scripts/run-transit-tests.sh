#!/bin/bash
# Run Transit-specific tests

set -euo pipefail

echo "==================================="
echo "Running Transit Architecture Tests"
echo "==================================="

# Ensure we're in the project root
cd "$(dirname "$0")/.."

# Run protobuf generation if needed
if [ ! -d "target/classes/cmd" ]; then
    echo "Generating protobuf classes..."
    make proto
fi

# Compile Kotlin classes if needed
if [ ! -d "target/classes/potatoclient/kotlin/transit" ]; then
    echo "Compiling Kotlin Transit classes..."
    make compile-kotlin
fi

# Run Transit core tests
echo ""
echo "Running Transit core tests..."
clojure -M:test -n potatoclient.transit-core-test

# Run Transit integration tests
echo ""
echo "Running Transit integration tests..."
clojure -M:test -n potatoclient.transit-integration-test

# Run specific Transit tests with pattern matching
echo ""
echo "Running all Transit-related tests..."
clojure -M:test --include "transit"

echo ""
echo "================================"
echo "Transit tests completed!"
echo "================================"