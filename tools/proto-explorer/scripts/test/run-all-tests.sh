#!/bin/bash

echo "========================================="
echo "Running Proto Explorer Tests"
echo "========================================="

FAILED=0

echo -e "\n1. Testing case preservation (simple-test.clj)..."
if clojure -M scripts/test/simple-test.clj; then
    echo "✓ Case preservation test passed"
else
    echo "✗ Case preservation test failed"
    FAILED=$((FAILED + 1))
fi

echo -e "\n2. Testing JSON parsing (test-json-parsing.clj)..."
if clojure -M scripts/test/test-json-parsing.clj; then
    echo "✓ JSON parsing test passed"
else
    echo "✗ JSON parsing test failed"
    FAILED=$((FAILED + 1))
fi

echo -e "\n3. Testing constraints extraction (test-constraints.clj)..."
if clojure -M scripts/test/test-constraints.clj > /dev/null 2>&1; then
    echo "✓ Constraints test passed"
else
    echo "✗ Constraints test failed"
    FAILED=$((FAILED + 1))
fi

echo -e "\n========================================="
if [ $FAILED -eq 0 ]; then
    echo "All tests passed!"
    exit 0
else
    echo "$FAILED test(s) failed"
    exit 1
fi