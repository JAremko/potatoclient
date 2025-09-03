#!/bin/bash

# Create test logs directory with timestamp
TEST_RUN_DIR="/tmp/potatoclient-tests-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$TEST_RUN_DIR"
echo "$TEST_RUN_DIR"