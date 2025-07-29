#!/bin/bash
# Demonstrate that coverage works on a subset of namespaces

set -e

echo "=========================================="
echo "Coverage Demo - Core Namespaces Only"
echo "=========================================="

# Ensure we're in the project root
cd "$(dirname "$0")/.."

# Create demo coverage directory
mkdir -p target/demo-coverage

echo ""
echo "Running coverage on core namespaces (without protobuf dependencies)..."
echo ""

# Run cloverage on specific namespaces that don't depend on protobuf
clojure -M:test-coverage \
    -n "potatoclient.specs" \
    -n "potatoclient.theme" \
    -n "potatoclient.config" \
    -n "potatoclient.i18n" \
    -n "potatoclient.runtime" \
    -n "potatoclient.logging" \
    -n "potatoclient.state.schemas" \
    -t "potatoclient.proto-constraints-test" \
    -t "potatoclient.proto-validation-sanity-test" \
    -t "potatoclient.validation-boundary-test" \
    -t "potatoclient.validation-breakage-test" \
    --output "target/demo-coverage" \
    2>&1 | tee target/demo-coverage/coverage.log

echo ""
echo "=========================================="
echo "Demo Coverage Complete!"
echo "=========================================="
echo ""
echo "View report at: target/demo-coverage/index.html"
echo ""
echo "This demonstrates that coverage works when we exclude namespaces"
echo "that depend on protobuf classes. The full coverage would require"
echo "a more sophisticated classpath setup or running tests first to"
echo "ensure all classes are compiled."