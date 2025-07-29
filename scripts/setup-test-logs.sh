#!/bin/bash
# Setup test log directory structure

# Create logs directory if it doesn't exist
mkdir -p logs/test-runs

# Create timestamp for this test run
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
TEST_RUN_DIR="logs/test-runs/${TIMESTAMP}"
mkdir -p "${TEST_RUN_DIR}"

# Export for use in other scripts
echo "${TEST_RUN_DIR}"