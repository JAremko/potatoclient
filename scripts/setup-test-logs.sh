#!/bin/bash
# Simple script to setup test logs directory
# Create a test run directory
mkdir -p logs/test-runs
TEST_DIR="logs/test-runs/test-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$TEST_DIR"
echo "$TEST_DIR"