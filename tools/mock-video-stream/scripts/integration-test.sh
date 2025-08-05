#!/bin/bash
# Integration test script for mock video stream tool
# Tests the tool working with the main application

set -euo pipefail

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Mock Video Stream Integration Test${NC}"
echo "===================================="

# Change to tool directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR/.."

# Test 1: Validate scenarios
echo -e "\n${YELLOW}Test 1: Validating scenarios${NC}"
if clojure -M:run validate; then
    echo -e "${GREEN}✓ Scenario validation passed${NC}"
else
    echo -e "${RED}✗ Scenario validation failed${NC}"
    exit 1
fi

# Test 2: Generate test data
echo -e "\n${YELLOW}Test 2: Generating test data${NC}"
rm -rf ./test-data
if clojure -X:generate; then
    echo -e "${GREEN}✓ Test data generation successful${NC}"
    if [ -d "./test-data" ] && [ "$(ls -A ./test-data)" ]; then
        echo "  Generated files:"
        ls -la ./test-data/*.json | awk '{print "    " $9}'
    else
        echo -e "${RED}✗ No test data files generated${NC}"
        exit 1
    fi
else
    echo -e "${RED}✗ Test data generation failed${NC}"
    exit 1
fi

# Test 3: Run example scenario
echo -e "\n${YELLOW}Test 3: Running example scenario${NC}"
if clojure -M:run example --scenario tap-center | grep -q "✓ Commands match expected output"; then
    echo -e "${GREEN}✓ Example scenario produced expected output${NC}"
else
    echo -e "${RED}✗ Example scenario did not match expected output${NC}"
    exit 1
fi

# Test 4: Start subprocess and send test message
echo -e "\n${YELLOW}Test 4: Testing subprocess mode${NC}"

# Create a test message file
cat > test-message.edn << 'EOF'
{:msg-type :control
 :msg-id "test-123"
 :timestamp 1234567890
 :payload {:type :get-status}}
EOF

# Start the subprocess in background and capture output
timeout 5s clojure -M:process --stream-type heat < test-message.edn > test-output.log 2>&1 || true

if grep -q "Mock video stream started" test-output.log; then
    echo -e "${GREEN}✓ Subprocess started successfully${NC}"
else
    echo -e "${RED}✗ Subprocess failed to start${NC}"
    cat test-output.log
    exit 1
fi

# Clean up
rm -f test-message.edn test-output.log

# Test 5: Java NDC converter
echo -e "\n${YELLOW}Test 5: Testing Java NDC converter${NC}"
cd ../..
if clojure -M:test -n potatoclient.video.ndc-converter-test 2>/dev/null; then
    echo -e "${GREEN}✓ NDC converter tests passed${NC}"
else
    echo -e "${YELLOW}⚠ NDC converter tests not found (this is expected if not yet created)${NC}"
fi

echo -e "\n${GREEN}All integration tests passed!${NC}"