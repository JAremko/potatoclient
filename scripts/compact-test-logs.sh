#!/bin/bash
# Compact test logs into a summary report

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if log file is provided
if [ $# -eq 0 ]; then
    echo "Usage: $0 <test-log-file>"
    exit 1
fi

LOG_FILE="$1"
if [ ! -f "$LOG_FILE" ]; then
    echo "Error: Log file '$LOG_FILE' not found"
    exit 1
fi

# Create summary file
SUMMARY_FILE="${LOG_FILE%.log}-summary.txt"
FAILURES_FILE="${LOG_FILE%.log}-failures.txt"

echo "Test Run Summary" > "$SUMMARY_FILE"
echo "================" >> "$SUMMARY_FILE"
echo "Log file: $LOG_FILE" >> "$SUMMARY_FILE"
echo "Generated: $(date)" >> "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"

# Extract key metrics
echo "## Overall Results" >> "$SUMMARY_FILE"
echo "-----------------" >> "$SUMMARY_FILE"
grep -E "Ran .* tests containing .* assertions" "$LOG_FILE" | tail -1 >> "$SUMMARY_FILE" || echo "No test summary found" >> "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"

# Extract namespaces with failures/errors
echo "## Test Namespaces with Issues" >> "$SUMMARY_FILE"
echo "------------------------------" >> "$SUMMARY_FILE"
grep -B5 "^Testing " "$LOG_FILE" | grep -A5 -E "(FAIL|ERROR) in" | grep "^Testing " | sort | uniq >> "$SUMMARY_FILE" || echo "No namespace information found" >> "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"

# Count failures by test
echo "## Failure Summary by Test" >> "$SUMMARY_FILE"
echo "--------------------------" >> "$SUMMARY_FILE"
grep -E "^(FAIL|ERROR) in" "$LOG_FILE" | sed 's/ (.*//' | sort | uniq -c | sort -nr >> "$SUMMARY_FILE" || echo "No failures found" >> "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"

# Extract unique failure types
echo "## Unique Failure Types" >> "$SUMMARY_FILE"
echo "----------------------" >> "$SUMMARY_FILE"
grep -A2 -E "^(FAIL|ERROR) in" "$LOG_FILE" | grep -E "expected:|actual:|Exception|Error" | sort | uniq -c | sort -nr | head -20 >> "$SUMMARY_FILE" || echo "No failure details found" >> "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"

# Extract compilation errors if any
echo "## Compilation Errors" >> "$SUMMARY_FILE"
echo "--------------------" >> "$SUMMARY_FILE"
grep -E "(ClassNotFoundException|CompilerException|Syntax error)" "$LOG_FILE" | head -10 >> "$SUMMARY_FILE" || echo "No compilation errors found" >> "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"

# Extract WebSocket/connection errors
echo "## Connection/WebSocket Issues" >> "$SUMMARY_FILE"
echo "-----------------------------" >> "$SUMMARY_FILE"
grep -iE "(websocket|connection|address already in use|connection refused)" "$LOG_FILE" | sort | uniq -c | sort -nr | head -10 >> "$SUMMARY_FILE" || echo "No connection issues found" >> "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"

# Create detailed failures file
echo "Detailed Test Failures" > "$FAILURES_FILE"
echo "=====================" >> "$FAILURES_FILE"
echo "" >> "$FAILURES_FILE"

# Extract each failure with context
grep -B3 -A10 -E "^(FAIL|ERROR) in" "$LOG_FILE" >> "$FAILURES_FILE" || echo "No detailed failures found" >> "$FAILURES_FILE"

# Generate console summary
echo -e "${BLUE}Test Log Analysis Complete${NC}"
echo -e "${BLUE}=========================${NC}"
echo ""

# Show key metrics
TOTAL_TESTS=$(grep -E "Ran .* tests" "$LOG_FILE" | tail -1 | awk '{print $2}')
TOTAL_ASSERTIONS=$(grep -E "Ran .* tests" "$LOG_FILE" | tail -1 | awk '{print $5}')
FAILURES=$(grep -E "Ran .* tests" "$LOG_FILE" | tail -1 | awk '{print $7}')
ERRORS=$(grep -E "Ran .* tests" "$LOG_FILE" | tail -1 | awk '{print $9}')

if [ -n "$TOTAL_TESTS" ]; then
    echo -e "Total Tests:      ${GREEN}$TOTAL_TESTS${NC}"
    echo -e "Total Assertions: ${GREEN}$TOTAL_ASSERTIONS${NC}"
    
    if [ "$FAILURES" != "0" ] && [ -n "$FAILURES" ]; then
        echo -e "Failures:         ${RED}$FAILURES${NC}"
    else
        echo -e "Failures:         ${GREEN}0${NC}"
    fi
    
    if [ "$ERRORS" != "0" ] && [ -n "$ERRORS" ]; then
        echo -e "Errors:           ${RED}$ERRORS${NC}"
    else
        echo -e "Errors:           ${GREEN}0${NC}"
    fi
else
    echo -e "${YELLOW}No test summary found in log file${NC}"
fi

echo ""
echo -e "Summary saved to: ${GREEN}$SUMMARY_FILE${NC}"
echo -e "Failures saved to: ${GREEN}$FAILURES_FILE${NC}"

# Show top failures
echo ""
echo -e "${YELLOW}Top 5 Failing Tests:${NC}"
grep -E "^(FAIL|ERROR) in" "$LOG_FILE" | sed 's/ (.*//' | sort | uniq -c | sort -nr | head -5 | while read count type test; do
    echo -e "  ${RED}$count${NC} $type $test"
done

# Create a one-line summary for CI/CD
ONELINE_SUMMARY="${LOG_FILE%.log}-oneline.txt"
if [ -n "$TOTAL_TESTS" ]; then
    echo "Tests: $TOTAL_TESTS | Assertions: $TOTAL_ASSERTIONS | Failures: ${FAILURES:-0} | Errors: ${ERRORS:-0}" > "$ONELINE_SUMMARY"
else
    echo "Test run failed or incomplete" > "$ONELINE_SUMMARY"
fi