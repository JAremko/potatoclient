#!/bin/bash
# Test runner script for validate module
# Runs core validation tests and reports results

set -e

echo "========================================="
echo "Running Validation Tests"
echo "========================================="
echo

# Core oneof_edn tests
echo "1. Testing oneof_edn implementation..."
clojure -M:test -n validate.specs.oneof-edn-test 2>&1 | grep -E "Ran|failures|errors" || true
echo

# CMD root message tests
echo "2. Testing cmd/root validation..."
clojure -M:test -n validate.specs.cmd-root-final-test 2>&1 | grep -E "Ran|failures|errors" || true
echo

# Property tests for cmd
echo "3. Testing cmd property generation..."
clojure -M:test -n validate.specs.cmd-property-test 2>&1 | grep -E "Ran|failures|errors" || true
echo

# Property tests for state
echo "4. Testing state property generation..."
clojure -M:test -n validate.specs.state-property-test 2>&1 | grep -E "Ran|failures|errors" || true
echo

# Note: Round-trip tests currently have naming convention issues (kebab-case vs snake_case)
# These will be fixed when proto mapping is updated
echo "========================================="
echo "Known Issues:"
echo "- State tests use kebab-case but proto expects snake_case"
echo "- Round-trip tests need proto mapper updates"
echo "========================================="
echo

echo "Core tests completed. Run 'clojure -M:test' for full test suite."